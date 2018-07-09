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
import uk.ac.uea.cmp.spectre.core.ds.distance.DistanceMatrix;
import uk.ac.uea.cmp.spectre.core.ds.distance.FlexibleDistanceMatrix;
import uk.ac.uea.cmp.spectre.lasso.LassoDistanceGraph;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Locates a maximal chordal subgraph, and then splits this into a number of distinct stable triplet covers
 * which can be resolved to an unrooted tree
 */
public class TripletCoverFinder {
    LassoDistanceGraph graph;
    public TripletCoverFinder(LassoDistanceGraph graph) {
        this.graph = graph;
    }

    public List<DistanceMatrix> findTripletCovers() {
        //Split input into connected components
        List<LassoDistanceGraph> components = this.graph.getConnectedComponents();
        List<DistanceMatrix> triplets = new ArrayList<>();
        ChordalSubgraphFinder chordalFinder = new ChordalSubgraphFinder();
        for(LassoDistanceGraph component : components) {
            //For each connected component get the maximal chordal subgraph
            LassoDistanceGraph chordalSub = chordalFinder.find(component);
            //Remove any loose edges (not part of a triangle)
            removeLooseEdges(chordalSub);
            //Start at a random degree 2 vertex
            Optional<Identifier> start = getStartVertex(chordalSub);
            while (start.isPresent()) {
                LassoDistanceGraph tripletCover = tripletSearch(start.get(), chordalSub);
                triplets.add(tripletCover);
                //Continue to get triplet cover sections until no degree 2 vertex exists
                start = getStartVertex(chordalSub);
            }
        }
        return triplets;
    }

    private LassoDistanceGraph tripletSearch(Identifier start, LassoDistanceGraph graph) {
        Queue<Pair<Identifier, Identifier>> queue = new LinkedList<>();
        List<Pair<Identifier, Identifier>> visited = new ArrayList<>();
        LassoDistanceGraph tripletCover = new LassoDistanceGraph(new FlexibleDistanceMatrix());
        Set<Identifier> triangle = graph.getNeighbours(start);
        triangle.add(start);
        Set<Pair<Identifier, Identifier>> edges = allPairs(triangle);
        for(Pair<Identifier, Identifier> edge : edges) {
            queue.add(edge);
            visited.add(edge);
        }
        while(queue.size() > 0) {
            Pair<Identifier, Identifier> edge = queue.poll();
            //add to triplet cover
            addTaxon(edge.getLeft(), tripletCover);
            addTaxon(edge.getRight(), tripletCover);
            tripletCover.setDistance(edge.getLeft(), edge.getRight(), graph.getDistance(edge.getLeft(),edge.getRight()));
            //Find any triangles which form with this edge
            Set<Identifier> triangles = graph.getNeighbours(edge.getRight());
            triangles.retainAll(graph.getNeighbours(edge.getLeft()));
            //Each vertex adjacent to both ends of this edge will form a triangle.
            //Enqueue any unvisited edges of this triangle
            for(Identifier v: triangles) {
                Set<Identifier> vertices = new HashSet<>();
                vertices.add(edge.getLeft());
                vertices.add(edge.getRight());
                vertices.add(v);
                edges = allPairs(vertices);
                for(Pair<Identifier, Identifier> e: edges) {
                    if(!visited.contains(e)) {
                        queue.add(e);
                        visited.add(e);
                    }
                }
            }
        }
        //Remove the visisted edges from the source graph
        visited.forEach(edge -> graph.removeDistance(edge.getLeft(), edge.getRight()));
        return tripletCover;
    }

    private Set<Pair<Identifier, Identifier>> allPairs(Set<Identifier> set) {
        IdentifierList list = new IdentifierList();
        Set<Pair<Identifier, Identifier>> edges = new HashSet<>();
        for(Identifier vertex : set) {
            list.add(vertex);
        }
        IdentifierCombinations combinations = new IdentifierCombinations(list, 2);
        for(IdentifierList edge : combinations) {
            edges.add(new ImmutablePair<>(edge.get(0), edge.get(1)));
        }
        return edges;
    }

    private void addTaxon(Identifier taxon, LassoDistanceGraph graph) {
        if(!graph.getTaxa().contains(taxon)) {
            graph.addIdentifierWithoutDistances(taxon);
        }
    }

    private Optional<Identifier> getStartVertex(LassoDistanceGraph graph) {
         return graph.getTaxa().stream()
                .filter(vertex -> graph.getNeighbours(vertex).size() == 2)
                .findFirst();
    }

    private void removeLooseEdges(LassoDistanceGraph graph) {
        //Remove any loose edges (not part of a triangle)
        Set<Pair<Identifier, Identifier>> looseEdges = graph.getMap().entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(Map.Entry::getKey)
                .filter(edge -> !this.edgeInTriangle(edge, graph))
                .collect(Collectors.toSet());
        looseEdges.stream().forEach(graph::removeDistance);
    }

    private boolean edgeInTriangle(Pair<Identifier, Identifier> edge, LassoDistanceGraph graph) {
        //Determine if this edge forms part of a triangle
        Set<Identifier> leftNeighbour = graph.getNeighbours(edge.getLeft());
        leftNeighbour.retainAll(graph.getNeighbours(edge.getRight()));
        return leftNeighbour.size() > 0;
    }
}
