/*
 *  Suite of PhylogEnetiC Tools for Reticulate Evolution (SPECTRE)
 *  Copyright (C) 2017  UEA School of Computing Sciences
 *
 *  This program is free software: you can redistribute it and/or modify it under the term of the GNU General Public
 *  License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 *  later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program.  If not, see
 *  <http://www.gnu.org/licenses/>.
 */

package uk.ac.uea.cmp.spectre.lasso.unrooted;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.util.CombinatoricsUtils;
import uk.ac.uea.cmp.spectre.core.ds.Identifier;
import uk.ac.uea.cmp.spectre.core.ds.distance.FlexibleDistanceMatrix;
import uk.ac.uea.cmp.spectre.core.ds.quad.quartet.QuartetSystem;
import uk.ac.uea.cmp.spectre.lasso.LassoDistanceGraph;
import uk.ac.uea.cmp.spectre.lasso.LassoQuartets;

import java.util.*;
import java.util.stream.Collectors;

public class IncrementalLasso {
    private final LassoDistanceGraph graph;

    public IncrementalLasso(LassoDistanceGraph graph) {
        this.graph = graph;
    }

    /**
     * A class used to return a list of distances which can be inferred, and also indicate pair were found to be a cherry
     * Multiple different types of information we want to know on return, so custom class used.
     */
    private class InferResults {
        private List<Pair<Pair<Identifier, Identifier>, Double>> inferred;
        private Pair<Identifier, Identifier> cherry;

        InferResults() {
            this.inferred = new ArrayList<>();
            this.cherry = null;
        }

        public List<Pair<Pair<Identifier, Identifier>, Double>> getInferred() {
            return inferred;
        }

        public Pair<Identifier, Identifier> getCherry() {
            return this.cherry;
        }

        public void setCherry(Pair<Identifier, Identifier> cherry) {
            this.cherry = cherry;
        }

        void addResult(Pair<Identifier, Identifier> cord, Double distance) {
            this.inferred.add(new ImmutablePair<>(cord, distance));
        }

        boolean isCherry() {
            return cherry != null;
        }
    }

    public Pair<LassoDistanceGraph, Set<Pair<Identifier, Identifier>>> find() throws IllegalStateException {
        Set<Pair<Identifier, Identifier>> lasso = new HashSet<>();
        LassoDistanceGraph metric = new LassoDistanceGraph(new FlexibleDistanceMatrix());
        boolean terminate = false;

        List<Identifier> triangle = startTriangle();
        //Add these to metric, and to lasso
        augmentEdge(triangle.get(0), triangle.get(1), metric);
        augmentEdge(triangle.get(0), triangle.get(2), metric);
        augmentEdge(triangle.get(1), triangle.get(2), metric);
        addToLasso(triangle.get(0), triangle.get(1), metric, lasso);
        addToLasso(triangle.get(0), triangle.get(2), metric, lasso);
        addToLasso(triangle.get(1), triangle.get(2), metric, lasso);
        Set<Identifier> verticesAdded = triangle.stream().collect(Collectors.toSet());

        do {
            boolean inferFlag = false;
            //Loop through edges in input graph, find vertices which are adjacent to both ends of edge
            Set<Pair<Identifier, Identifier>> iterableCords = new HashSet<>(metric.getMap().keySet());
            for(Pair<Identifier, Identifier> cord : iterableCords) {
                Set<Identifier> candidates = intersectionNeighbourhood(cord.getLeft(), cord.getRight(), graph);
                candidates.removeAll(verticesAdded);
                //Remove and candidates already in metric
                //Loop through candidates and attempt to attach in metric
                Set<Identifier> iteratableCandidates = new HashSet<>(candidates);
                for(Identifier candidate: iteratableCandidates) {
                     InferResults inferrable = inferDistances(candidate, cord, metric);
                     if(!inferrable.isCherry() && !inferrable.getInferred().stream().filter(p -> p.getRight() <= 0).findFirst().isPresent()) {
                         //exlcude this vertex from consideration in the future
                         verticesAdded.add(candidate);
                         candidates.remove(candidate);
                         //add all these edges to our metric, and lasso
                         augmentEdge(cord.getLeft(), candidate, metric);
                         augmentEdge(cord.getRight(), candidate, metric);
                         addToLasso(cord.getRight(), candidate, metric, lasso);
                         addToLasso(cord.getLeft(), candidate, metric, lasso);
                         //add inferred edges
                         inferrable.getInferred().stream().forEach(res -> {
                             metric.setDistance(res.getLeft().getLeft().getName(), res.getLeft().getRight().getName(), res.getRight());
                         });
                         inferFlag = true;
                     } else {
                         //TODO: Add code to attempt to attach to cords of form (candidate, other cherry vertex)
                     }
                }
            }
            terminate = !inferFlag;
        } while(!terminate);
        return new ImmutablePair<>(metric, lasso);
    };

    private void addToLasso(Identifier v1, Identifier v2, LassoDistanceGraph metric, Set<Pair<Identifier, Identifier>> lasso) {
        lasso.add(sortedPair(metric.getTaxa().getByName(v1.getName()), metric.getTaxa().getByName(v2.getName())));

    }

    private List<Identifier> startTriangle() throws IllegalStateException {
        //Order by edge weight, always want lowest weight select to get cherry edge weights
        List<Pair<Identifier, Identifier>> orderedEdges = graph.getMap().entrySet().stream()
                .sorted((a,b) -> a.getValue().compareTo(b.getValue()))
                .map(e -> e.getKey())
                .collect(Collectors.toList());
        for (Pair<Identifier, Identifier> edge : orderedEdges) {
            Set<Identifier> intersectEdge = intersectionNeighbourhood(edge.getLeft(), edge.getRight(), graph);
            Identifier selected = null;
            if (intersectEdge.size() > 1) {
                //locate vertex with lowest distance from the cluster induced by deleting the hypothetical internal vertex
                Double minDist = null;
                for(Identifier v: intersectEdge) {
                    double distFromInternal = (graph.getDistance(edge.getRight(), v)
                            + graph.getDistance(edge.getLeft(), v) - graph.getDistance(edge.getRight(), edge.getLeft())) / 2;
                    if(minDist == null)
                        minDist = distFromInternal + 1;
                    if(distFromInternal < minDist) {
                        selected = v;
                        minDist = distFromInternal;
                    }
                }
                if(selected != null) {
                    //Return the first located triangle
                    List<Identifier> triangle = new ArrayList<>();
                    triangle.add(selected);
                    triangle.add(edge.getRight());
                    triangle.add(edge.getLeft());
                    return triangle;
                }
            }
        }
        //No triangles exist in the graph - bad news
        throw new IllegalStateException("No clique of 3 exists in the input distances");
    }

    /**
     * Find all vertices which neighbour both v1 and v2 in graph
     * @param v1 Vertex one
     * @param v2 Vertex two
     * @param graph The graph in v1 and v2 are vertices
     * @return Set of vertices
     */
    private Set<Identifier> intersectionNeighbourhood(Identifier v1, Identifier v2, LassoDistanceGraph graph) {
        Identifier localv1 = graph.getTaxa().getByName(v1.getName());
        Identifier localv2 = graph.getTaxa().getByName(v2.getName());
        Set<Identifier> neighbours = graph.getNeighbours(localv1);
        neighbours.retainAll(graph.getNeighbours(localv2));
        return neighbours;
    }

    /**
     * Set the edge in a graph to be the same weight as in the source graph
     * @param v1
     * @param v2
     * @param graph
     */
    private void augmentEdge(Identifier v1, Identifier v2, LassoDistanceGraph graph) {
        if(!graph.getTaxa().containsName(v1.getName()))
            graph.getTaxa().add(v1);
        if(!graph.getTaxa().containsName(v2.getName()))
            graph.getTaxa().add(v2);
        graph.setDistance(v1.getName(), v2.getName(), this.graph.getDistance(v1.getName(), v2.getName()));
    }

    /**
     * Return any missing distances which can be inferred in the graph provided. This terminates early upon
     * encountering a distance which cannot be inferred. Distances will not be added to graph.
     * @param vertex Vertex to infer distances between
     * @param pivots Pair of vertices to act as pivots
     * @param graph The graph with distances to infer based on.
     * @return A list of any distances which can be inferred
     */
    private InferResults inferDistances(Identifier vertex, Pair<Identifier, Identifier> pivots, LassoDistanceGraph graph) {
        InferResults results = new InferResults();
        //For every vertex adjacen to pivots in graph, attempt to infer distance. Distance for pivots to vertex
        //come from source graph
        //Let x, y be pivot vertices. a is vertex, and b is every vertex in I_x,y
        Identifier x = pivots.getLeft();
        Identifier y = pivots.getRight();
        Identifier a = vertex;
        double ax = this.graph.getDistance(a.getName(), x.getName());
        double ay = this.graph.getDistance(a.getName(), y.getName());
        double xy = graph.getDistance(x, y);
        for(Identifier b : intersectionNeighbourhood(x, y, graph)) {
            double bx = graph.getDistance(x, b);
            double by = graph.getDistance(b, y);
            if(validDiamond(ax, by, bx, ay, xy)) {
                double distance = Math.max(ax + by, bx + ay) - xy;
                results.addResult(sortedPair(a, b), Math.max(ax + by, bx + ay) - xy);
            } else {
                results.setCherry(sortedPair(a,b));
                break;
            }
        }
        return results;
    }

    private boolean validDiamond(double ay, double bx, double ax, double by, double xy) {
        if(ax + by == bx + ay)
            return false;
        double ab = Math.max(ay + bx, ax + by) - xy;
        //ensure no negative branch lengths
//        double uv = (Math.max(ay + bx, ax + by) - Math.min(ay + bx, ax + by)) / 2;
//        double au = (ab + ax - bx) / 2;
//        return triangle(ay, by, ab) && triangle(ax, ab, bx) && triangle(xy, by, bx) && triangle(ab, ax, bx);
        //relabel for convenience - cd|ef will be cherries
        double cd, ef, ce, cf, de, df;
        if(ay + bx > ax + by) {
            cd = by;
            ef = ax;
            de = xy;
            df = ay;
            ce = bx;
            cf = xy;

        } else {
            cd = bx;
            ef = ay;
            de = ax;
            df = ab;
            ce = xy;
            cf = by;
        }
        boolean tri = 0.5 * (ce + df + cf + de) >= cd + ef;
        //ensure no impossible edge weights
        double uv = (ce + df - cd - ef) / 2;
        double cu = ((ce + cf - ef) / 2) - uv;
        double du = cd - cu;
        double ev = ce - uv - cu;
        double fv = ef - ev;
        return tri && uv > 0 && cu > 0 && du > 0 && ev > 0 && fv > 0;
    }

    private boolean triangle(double edge1, double edge2, double edge3) {
        return edge1 <= edge2 + edge3 || edge3 <= edge1 + edge2 || edge3 <= edge1 + edge2;
    }

    /**
     * Return a pair object with the two vertices, with lower id first in the pair
     * @param v1 Vertex
     * @param v2 Vertex
     * @return A pair with lowest id vertex in left
     */
    private Pair<Identifier, Identifier> sortedPair(Identifier v1, Identifier v2) {
        return new ImmutablePair<>(
                v1.getId() < v2.getId() ? v1: v2,
                v1.getId() < v2.getId() ? v2: v1
        );
    }
}
