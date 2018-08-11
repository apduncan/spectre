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
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ChordalSubgraphFinder {
    private LassoDistanceGraph graph;
    private LassoDistanceGraph subgraph;
    public final static SEED_TREE SEED_DEFAULT = SEED_TREE.BREADTH;
    public enum SEED_TREE {
        DEPTH, BREADTH
    };

    public ChordalSubgraphFinder(LassoDistanceGraph graph) {
        this.graph = new LassoDistanceGraph(graph);
    }

    /**
     * Locates a maximal chordal subgraph of the input graph, return the subgraph.
     * @return A maximal chordal subgraph of the input
     */
    public LassoDistanceGraph find(SEED_TREE seedTree) {
        //Initialise the subgraph to a spanning tree of the source graph, this will be chordal
        TreeAccumulator bfsTree = new TreeAccumulator(this.graph);
        if(seedTree == SEED_TREE.BREADTH)
            this.graph.breadthFirstSearch(this.graph.getTaxa().getFirst(), null, bfsTree, null);
        if(seedTree == SEED_TREE.DEPTH)
            this.graph.depthFirstSearch(this.graph.getTaxa().getFirst(), new ArrayList<Identifier>(), null, bfsTree);
        this.subgraph = bfsTree.getBfsTree();
        Set<Pair<Identifier, Identifier>> candidateEdges = new HashSet<>();
        for(Identifier vertex : subgraph.getTaxa()) {
            addCandidateEdges(vertex, candidateEdges);
        }

        while(candidateEdges.size() > 0) {
            //Select a random edge from candidates
            Pair<Identifier, Identifier> edge = candidateEdges.stream().findFirst().get();
            candidateEdges.remove(edge);
            if(candidateEdgeCheck(edge)) {
                subgraph.setDistance(edge.getLeft(), edge.getRight(), graph.getDistance(edge.getLeft(), edge.getRight()));
                Identifier[] vertices = {edge.getRight(), edge.getLeft()};
                for(Identifier vertex : vertices) {
                    addCandidateEdges(vertex, candidateEdges);
                }
            }

        }
        return subgraph;
    }

    public LassoDistanceGraph find() {
        return this.find(SEED_DEFAULT);
    }

    /**
     * Check if edge can be added to the chordal subgraph, and the result graph will still be chordal, and will be
     * a strong lasso
     * @param edge Edge to add
     * @return True if can be added
     */
    private boolean candidateEdgeCheck(Pair<Identifier, Identifier> edge) {
        //Get vertex v which is adjacent to both ends of edge in subgraph
        Set<Identifier> neighbours = subgraph.getNeighbours(edge.getRight());
        neighbours.retainAll(subgraph.getNeighbours(edge.getLeft()));
        Identifier v = neighbours.stream().findFirst().get();
        //Find vertices which are adjacent to v, but not adjacent to both ends of edge
        Set<Identifier> vNeighbours = subgraph.getNeighbours(v);
        vNeighbours.remove(v);
        //Find if a path exists between the two ends of the edge, when subgraph is resritced to vNeighbours
        boolean path = pathExists(edge.getLeft(), edge.getRight(), subgraph, vNeighbours);
        //If a path exists, this is not a valid edge to add
        if(path)
            return false;
        //Locate any diamonds formed by adding edge ab to the subgraph. Only accept if all these diamonds
        //fit the conditions which would allow the weight of the missing edge to be inferred (the missing edge is
        //between a pair not in the same cherry)
        List<Pair<Identifier, Identifier>> triangleEdges = new ArrayList<>();
        triangleEdges.add(new ImmutablePair<>(v, edge.getLeft()));
        triangleEdges.add(new ImmutablePair<>(v, edge.getRight()));
        for(Pair<Identifier, Identifier> triedge : triangleEdges) {
            //Check if a matching triangle exists on the other side of this edge.
            //va will not return b as a neighbour, as edge ab does not exist in subgraph yet
            Set<Identifier> neighbourIntersection = subgraph.getNeighbours(triedge.getLeft());
            neighbourIntersection.retainAll(subgraph.getNeighbours(triedge.getRight()));
            if(neighbourIntersection.size() > 0) {
                //find out which vertex of candidate edge is not involved in the cord
                Identifier y = triedge.getRight().equals(edge.getRight()) ? edge.getLeft() : edge.getRight();
                Identifier x = triedge.getRight();
                //check that it meets the criteria for an acceptable diamond.
                //cord uy is unknown, xv is known
                //For a diamond to be resolved, the cycle (diamond less the chord) must be skew
                //For cycle {uvyx}, skew if d(u,v) + d(x,y) != d(v,y) + d(u,x)
                //Either side of the inequality is a parallel edge in the diamond
                Identifier u = neighbourIntersection.stream().findFirst().get();
                Double duv = graph.getDistance(u, v);
                Double dxy = graph.getDistance(x, y);
                Double dvy = graph.getDistance(v, y);
                Double dux = graph.getDistance(u, x);
                if(duv + dxy == dvy + dux)
                    return false;
                //Needs to resolve to a distance > 0
                if((Math.max(duv + dxy, dvy + dux) - graph.getDistance(u,y)) == 0)
                    return false;
            }
        }
        //No reason to reject the addition of this edge have been found, so accept
        return true;
    }

    /**
     * Find if a path exists between two vertices in a graph, or a subgraph induced by restricting to certain vertices.
     * @param from Vertex to start searching from
     * @param to Vertex to find a path to
     * @param graph The graph to find the path in
     * @param restrict Search in a subgraph induced by restricting graph to contain only vertices in restrict
     * @return Boolean
     */
    private boolean pathExists(Identifier from, Identifier to, LassoDistanceGraph graph, Set<Identifier> restrict) {
        if(restrict == null) {
            restrict = new HashSet<>(graph.getTaxa());
        }
        Queue<Identifier> queue = new LinkedList<>();
        List<Identifier> visited = new ArrayList<>();
        queue.add(from);
        visited.add(from);
        while(queue.size() > 0) {
            if(visited.contains(to))
                return true;
            Identifier v = queue.poll();
            for(Identifier neighbour : graph.getNeighbours(v)) {
                if(!visited.contains(neighbour) && restrict.contains(neighbour)) {
                    queue.add(neighbour);
                    visited.add(neighbour);
                }
            }
        }
        return false;
    }

    /**
     * Find possible edges for expand the subgraph from a vertex
     * @param vertex Vertex to expand from
     * @param candidates The set of candidate edges
     */
    private void addCandidateEdges(Identifier vertex, Set<Pair<Identifier, Identifier>> candidates) {
        //Candidate edges are edges ab, where a & b are adjacent to vertex in subgraph,
        // and ab is in edges(graph) - edges(subgraph)
        Set<Identifier> neighbours = subgraph.getNeighbours(vertex);
        IdentifierList neighbourList = new IdentifierList();
        neighbours.forEach(neighbourList::add);
        //Identify all possible edges formed by neighbours
        if(neighbours.size() > 1) {
            Set<Pair<Identifier, Identifier>> edges = new HashSet<>();
            IdentifierCombinations pairCombinations = new IdentifierCombinations(neighbourList, 2);
            for (IdentifierList pair : pairCombinations) {
                edges.add(new ImmutablePair<>(pair.get(0), pair.get(1)));
            }
            //For each edge which exists in graph, and not in subgraph, add to candidates
            edges.stream()
                    .filter(edge -> graph.getDistance(edge.getLeft(), edge.getRight()) > 0)
                    .filter(edge -> subgraph.getDistance(edge.getLeft(), edge.getRight()) == 0)
                    .forEach(candidates::add);
        }
    }

    /**
     * Builds a tree when passed to the breadth first search method of LassoDistanceGraph
     */
    private class TreeAccumulator implements Consumer<Pair<Identifier, Identifier>> {
        private LassoDistanceGraph source;
        private LassoDistanceGraph bfsTree;

        public LassoDistanceGraph getBfsTree() {
            return bfsTree;
        }

        public TreeAccumulator(LassoDistanceGraph source) {
            this.source = source;
            this.bfsTree = new LassoDistanceGraph(new FlexibleDistanceMatrix(source.getTaxa()));
        }

        @Override
        public void accept(Pair<Identifier, Identifier> edge) {
            //Add this traversed edge to the breadth first tree
            bfsTree.setDistance(edge.getLeft(), edge.getRight(), source.getDistance(edge.getLeft(), edge.getRight()));
        }
    }
}
