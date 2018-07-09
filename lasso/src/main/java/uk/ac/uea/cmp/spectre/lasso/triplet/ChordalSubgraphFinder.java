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

package uk.ac.uea.cmp.spectre.lasso.triplet;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import uk.ac.uea.cmp.spectre.core.ds.Identifier;
import uk.ac.uea.cmp.spectre.core.ds.IdentifierList;
import uk.ac.uea.cmp.spectre.core.ds.distance.FlexibleDistanceMatrix;
import uk.ac.uea.cmp.spectre.lasso.LassoDistanceGraph;

import java.util.*;
import java.util.function.Consumer;

public class ChordalSubgraphFinder {
    private LassoDistanceGraph graph;
    private LassoDistanceGraph subgraph;

    public ChordalSubgraphFinder() {
    }

    /**
     * Locates a maximal chordal subgraph of the input graph, return the subgraph.
     * @param graph The supergraph
     * @return A maximal chordal subgraph of the input
     */
    public LassoDistanceGraph find(LassoDistanceGraph graph) {
        this.graph = graph;
        //Initialise the subgraph to a spanning tree of the source graph, this will be chordal
        TreeAccumulator bfsTree = new TreeAccumulator(this.graph);
        this.graph.breadthFirstSearch(this.graph.getTaxa().getFirst(), null, bfsTree, null);
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

    /**
     * Check if edge can be added to the chordal subgraph, and the result graph will still be chordal
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
        return !pathExists(edge.getLeft(), edge.getRight(), subgraph, vNeighbours);
    }

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
