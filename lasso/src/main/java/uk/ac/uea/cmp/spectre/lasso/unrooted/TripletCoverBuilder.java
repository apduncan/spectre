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
import uk.ac.uea.cmp.spectre.core.ds.Identifier;
import uk.ac.uea.cmp.spectre.core.ds.IdentifierList;
import uk.ac.uea.cmp.spectre.core.ds.distance.FlexibleDistanceMatrix;
import uk.ac.uea.cmp.spectre.lasso.IdentifierCombinations;
import uk.ac.uea.cmp.spectre.lasso.LassoDistanceGraph;

import java.util.*;
import java.util.stream.Collectors;

public class TripletCoverBuilder {
    private LassoDistanceGraph graph;

    public TripletCoverBuilder(LassoDistanceGraph graph) {
        this.graph = new LassoDistanceGraph(graph);
    }

    /***
     * Locate a maximal stable triplet cover subgraph of graph
     * @return
     */
    /*public LassoDistanceGraph find() {
        LinkedList<Pair<Identifier, Identifier>> freeEdges = new LinkedList<>();
        Set<Pair<Identifier, Identifier>> usedEdges = new HashSet<>();
        IdentifierList forbiddenCandidates = new IdentifierList();
        LassoDistanceGraph cover = new LassoDistanceGraph(new FlexibleDistanceMatrix());
        //Find a starting diamond
        List<Identifier> start = startTriangle();
        Pair<Identifier, Identifier> tri1 = new ImmutablePair<>(start.get(0), start.get(1));
        Pair<Identifier, Identifier> tri2 = new ImmutablePair<>(start.get(0), start.get(2));
        Pair<Identifier, Identifier> tri3 = new ImmutablePair<>(start.get(1), start.get(2));
        //Add all edges to the subgraph
        addEdge(tri1.getRight(), tri1.getLeft(), cover);
        addEdge(tri2.getRight(), tri2.getLeft(), cover);
        addEdge(tri3.getRight(), tri3.getLeft(), cover);
        //Add all edges currently in the graph as free (non-chordal) edges
        //The free edges to be use next should be the edge with minimal weight in the triangle
        Pair<Identifier, Identifier> minEdge = cover.getMap().keySet().stream()
                .min((a,b) -> Double.compare(graph.getDistance(a.getRight(), a.getLeft()), graph.getDistance(b.getRight(), b.getLeft()))).get();
        freeEdges.addAll(cover.getMap().keySet());
        freeEdges.remove(minEdge);
        Pair<Identifier, Identifier> midEdge = freeEdges.stream()
                .min((a,b) -> Double.compare(graph.getDistance(a.getRight(), a.getLeft()), graph.getDistance(b.getRight(), b.getLeft()))).get();
        freeEdges.removeIf(e -> true);
        freeEdges.add(midEdge);
        while(freeEdges.size() > 0) {
            //Select a free edge, see if a triangle can be formed which is a valid diamond
            Pair<Identifier, Identifier> chord = freeEdges.pollFirst();
            Optional<Identifier> v = intersectionNeighbourhood(chord.getLeft(), chord.getRight(), cover).stream().findFirst();
            Set<Identifier> candidates = intersectionNeighbourhood(chord.getLeft(), chord.getRight(), graph);
            candidates.remove(v.get());
            IdentifierList missing = new IdentifierList(graph.getTaxa());
            missing.removeAll(cover.getTaxa());
            missing.removeAll(candidates);
            missing.stream().filter(element -> !forbiddenCandidates.containsName(element.getName())).forEach(forbiddenCandidates::add);
            Pair<Identifier, Identifier> selectedEdge1 = null;
            Pair<Identifier, Identifier> selectedEdge2 = null;
            Double minDist = null;
            for(Identifier candidate: candidates) {
                if(!cover.getTaxa().containsName(candidate.getName()) && !forbiddenCandidates.containsName(candidate.getName())) {
                    Pair<Identifier, Identifier> missingChord = sortedPair(v.get(), candidate);
                    if (validDiamond(missingChord, chord)) {
                        //Want to select the diamond for which the added vertex will be closest in the
                        //cluster induced by removing the hypothetical internal vertex
                        //Add new edges (chord vertices to candidate) to graph and free edges
                        Pair<Identifier, Identifier> edge1 = sortedPair(candidate, chord.getRight());
                        Pair<Identifier, Identifier> edge2 = sortedPair(candidate, chord.getLeft());
                        double distFromInternal = (graph.getDistance(edge1.getRight(), edge1.getLeft()) + graph.getDistance(edge2.getRight(), edge2.getLeft()) - graph.getDistance(chord.getRight(), chord.getLeft())) / 2;
                        if(minDist == null)
                            minDist = distFromInternal + 1;
                        if(distFromInternal < minDist) {
                            selectedEdge1 = edge1;
                            selectedEdge2 = edge2;
                            minDist = distFromInternal;
                        }
                    }
                }
            }
            usedEdges.add(chord);
            if(selectedEdge1 != null) {
                freeEdges.removeIf(e -> true);
                //Want to check minimum edge weight first
                double weight1 = graph.getDistance(selectedEdge1.getRight(), selectedEdge1.getLeft());
                double weight2 = graph.getDistance(selectedEdge2.getRight(), selectedEdge2.getLeft());
                double weight3 = graph.getDistance(chord.getRight(), chord.getLeft());
                Pair<Identifier, Identifier> edgeUse;
                if(weight3 < weight2 && weight3 < weight1) {
                    //use the lesser of the new edges
                    if(weight1 == weight2) {
                         //Use the cord with lower degree
                        int degree1 = cover.getNeighbours(chord.getRight()).size();
                        int degree2 = cover.getNeighbours(chord.getLeft()).size();
                        edgeUse = degree1 < degree2 ? selectedEdge1 : selectedEdge2;
                    } else {
                        edgeUse = weight2 < weight1 ? selectedEdge2 : selectedEdge1;
                    }
                } else if(weight1 == weight2) {
                    //Use the cord with lower degree
                    int degree1 = cover.getNeighbours(chord.getRight()).size();
                    int degree2 = cover.getNeighbours(chord.getLeft()).size();
                    edgeUse = degree1 < degree2 ? selectedEdge1 : selectedEdge2;
                } else {
                    edgeUse = weight2 > weight1 ? selectedEdge2 : selectedEdge1;
                }
                freeEdges.add(edgeUse);
                addEdge(selectedEdge1.getRight(), selectedEdge1.getLeft(), cover);
                addEdge(selectedEdge2.getRight(), selectedEdge2.getLeft(), cover);
            }
        }
        return cover;
    }
    */

    public LassoDistanceGraph find() {
        List<Pair<Identifier, Identifier>> freeEdges = new ArrayList<>();
        Set<Identifier> excludedVertices = new HashSet<>();
        LassoDistanceGraph cover = new LassoDistanceGraph(new FlexibleDistanceMatrix());
        List<Identifier> triangle = startTriangle();
        //Scan the from the first two non minimum weight edges first
        freeEdges.add(sortedPair(triangle.get(0), triangle.get(1)));
        freeEdges.add(sortedPair(triangle.get(0), triangle.get(2)));
        Identifier shared = triangle.get(0);
        //add triangle to the cover
        addEdge(triangle.get(0), triangle.get(1), cover);
        addEdge(triangle.get(0), triangle.get(2), cover);
        addEdge(triangle.get(1), triangle.get(2), cover);
        boolean terminate = false;
        while(!terminate) {
            //Loop until no triangle added
            Pair<Identifier, Identifier> attachEdge = null;
            Pair<Identifier, Identifier> attachMissing = null;
            Identifier vertex = null;
            Set<Identifier> intersectEither = new HashSet<>();
            Double minDist = null;
            for(Pair<Identifier, Identifier> edge: freeEdges) {
                Set<Identifier> candidates = intersectionNeighbourhood(edge.getLeft(), edge.getRight(), graph);
                intersectEither.addAll(candidates);
                //Eliminate excluded vertices from consideration, and any vertices in the cover
                candidates.removeAll(excludedVertices);
                for(Identifier candidate: candidates) {
                    //Check if this is a valid diamond
                    Identifier m = intersectionNeighbourhood(edge.getLeft(), edge.getRight(), cover).stream().findFirst().get();
                    Pair<Identifier, Identifier> missingChord = sortedPair(m, candidate);
                    if(validDiamond(missingChord, edge) && !cover.getTaxa().containsName(candidate.getName())) {
                        //work out how far this is from the internal vertex which splits these clusters
                        //where ab is the chord, and c the candidate
                        double ab = graph.getDistance(edge.getRight(), edge.getLeft());
                        double ac = graph.getDistance(edge.getRight(), candidate);
                        double bc = graph.getDistance(edge.getLeft(), candidate);
                        double distFromInternal = (ac + bc - ab) / 2;

                        if(minDist == null)
                            minDist = distFromInternal + 1;
                        if(distFromInternal < minDist) {
                            minDist = distFromInternal;
                            attachEdge = edge;
                            attachMissing = missingChord;
                            vertex = candidate;
                        }
                    }
                }
            }
            if(vertex == null) {
                terminate = true;
            } else {
                //Infer the distance of the missing chord
                Identifier a = shared;
                Identifier b = attachEdge.getLeft() == shared ? attachEdge.getRight() : attachEdge.getLeft();
                Identifier x = attachMissing.getLeft() == vertex ? attachMissing.getRight() : attachMissing.getLeft();
                Identifier y = attachMissing.getLeft() != vertex ? attachMissing.getRight() : attachMissing.getLeft();
                double ay = graph.getDistance(a, y);
                double bx = graph.getDistance(b, x);
                double ax = graph.getDistance(a, x);
                double by = graph.getDistance(b, y);
                double ab = graph.getDistance(a, b);
                double xy = Math.max(ay + bx, ax + by) - ab;
                //Work out the distance of vertex from internal vertex shared between vertex,x,y
                Pair<Identifier, Identifier> otherEdge = freeEdges.get(0).getLeft().getName().equals(attachEdge.getLeft().getName()) &&
                        freeEdges.get(0).getRight().getName().equals(attachEdge.getRight().getName()) ? freeEdges.get(1) : freeEdges.get(0);
                double otherDistInternal = (xy + ay - ax) / 2;
                //y is to be the vertex being added
                //set a to one element of the chordal edge
                //set x to the other
                if(minDist < otherDistInternal) {
                    x = b;
                } else if (minDist == otherDistInternal) {
                    //minimise the sum of the created triangle
                    double aby = ay + ab + by;
                    double axy = ax + xy + ay;
                    if(aby < axy) {
                        x = b;
                    } else {
                        graph.setDistance(x, y, xy);
                    }
                } else {
                    graph.setDistance(x, y, xy);
                }
                //add vertex and edges to the cover
                Pair<Identifier, Identifier> edge1 = sortedPair(a, y);
                Pair<Identifier, Identifier> edge2 = sortedPair(x, y);
                addEdge(a, y, cover);
                addEdge(x, y, cover);
                shared = y;
                //add any vertices which were not neighbours of either edge to excluded edges
                Set<Identifier> allTaxa = graph.getTaxa().stream().collect(Collectors.toSet());
                allTaxa.removeAll(intersectEither);
                excludedVertices.addAll(allTaxa);
                //set the free edges for next iteration
                freeEdges = new ArrayList<>();
                freeEdges.add(edge1);
                freeEdges.add(edge2);
            }
        }
        return cover;
    }

    /***
     * Find the first valid diamond in graph
     * @return Pair (Missing Chord, Known Chord) in the diamond
     */
    private List<Identifier> startTriangle() {
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
        return null;
    }

    /**
     * Find all vertices which neighbour both v1 and v2 in graph
     * @param v1 Vertex one
     * @param v2 Vertex two
     * @param graph The graph in v1 and v2 are vertices
     * @return Set of vertices
     */
    private Set<Identifier> intersectionNeighbourhood(Identifier v1, Identifier v2, LassoDistanceGraph graph) {
        Set<Identifier> neighbours = graph.getNeighbours(v1);
        neighbours.retainAll(graph.getNeighbours(v2));
        return neighbours;
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

    private boolean validDiamond(Pair<Identifier, Identifier> missing, Pair<Identifier, Identifier> known) {
        /*
        The diamond
        x---u
        |  /|
        | / |
        |/  |
        v---y
        is valid if the sum of weight of parallel edges is not equal
        and the distance xy which can be inferred is non 0
        We don't care if edge xy exists in graph, we will be using it's inferred weight
         */
        Identifier x = missing.getLeft();
        Identifier y = missing.getRight();
        Identifier u = known.getLeft();
        Identifier v = known.getRight();

        double xu = graph.getDistance(x,u);
        double vy = graph.getDistance(v,y);
        double xv = graph.getDistance(x,v);
        double uy = graph.getDistance(u,y);

        if(xu + vy == xv + uy)
            return false;

        double uv = graph.getDistance(u,v);
        double xy = Math.max(xu + vy, xv + uy) - uv;

        if(xy == 0)
            return false;

        return true;
    }

    /**
     * Add and edge from graph to the graph specified in to
     * @param start One end of the edge
     * @param end One end of the edge
     * @param to The graph to add this edge to
     */
    private void addEdge(Identifier start, Identifier end, LassoDistanceGraph to) {
        double dist = graph.getDistance(start, end);
        //Add the vertices to the to graph if they do not exist
        to.addIdentifierWithoutDistances(start);
        to.addIdentifierWithoutDistances(end);
        to.setDistance(start, end, dist);
    }
}
