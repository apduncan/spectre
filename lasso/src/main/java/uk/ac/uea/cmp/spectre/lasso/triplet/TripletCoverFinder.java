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
    private LassoDistanceGraph graph;

    public TripletCoverFinder(LassoDistanceGraph graph) {
        this.graph = graph;
    }

    /**
     * Convert a potentially incomplete distance matrix into the minimal number of separate triplet covers.
     * @return A set of matrices representing triplet covers
     */
    public List<LassoDistanceGraph> findTripletCovers(ChordalSubgraphFinder.SEED_TREE seedMethod) {
        //Split input into connected components which are greater than size 3 (as need at least 4 to find a diamond)
        List<LassoDistanceGraph> components = this.graph.getConnectedComponents().stream()
                .filter(c -> c.getTaxa().size() > 3).collect(Collectors.toList());
        List<LassoDistanceGraph> triplets = new ArrayList<>();
        for(LassoDistanceGraph component : components) {
            //For each connected component get the maximal chordal subgraph
            LassoDistanceGraph chordalSub = new ChordalSubgraphFinder(component).find(seedMethod);
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
        return triplets.stream().filter(cover -> cover.getTaxa().size() > 3).collect(Collectors.toList());
    }

    public List<LassoDistanceGraph> findTripletCovers() {
        return findTripletCovers(ChordalSubgraphFinder.SEED_DEFAULT);
    }

    /**
     * Convert a chordal graph into a minimal number of triplet covers. When triangles joined at only one vertex, can be
     * split into a separate triplet cover. Removes all edges in the located triplet cover from the graph before
     * returning result.
     * @param start A vertex to start searching for a triplet cover from. Should be a vertex of degree 2.
     * @param graph The graph to search for a triplet cover in.
     * @return The triplet cover that contain start vertex.
     */
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

    /**
     * All pairwise combinations from a set of elements
     * @param set A set of Identifier to combine
     * @return Pair combinations of members of set
     */
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

    /**
     * Add a taxon to a graph without also adding a distance of 0 to each other taxon. Used to save time
     * when dynamically constructing graphs. If graph already contains taxon, it will not be added, all taxa must
     * be unique.
     * @param taxon Taxon to add
     * @param graph Graph taxon should be added to
     */
    private void addTaxon(Identifier taxon, LassoDistanceGraph graph) {
        if(!graph.getTaxa().contains(taxon)) {
            graph.addIdentifierWithoutDistances(taxon);
        }
    }

    /**
     * Find a valid vertex to start looking for a triplet cover from.
     * @param graph Graph to be searched
     * @return The first degree 2 vertex in graph
     */
    private Optional<Identifier> getStartVertex(LassoDistanceGraph graph) {
         return graph.getTaxa().stream()
                .filter(vertex -> graph.getNeighbours(vertex).size() == 2)
                .findFirst();
    }

    /**
     * Remove edges from a chordal graph which do not form part of a triangle.
     * @param graph Graph to be trimmed
     */
    private void removeLooseEdges(LassoDistanceGraph graph) {
        //Remove any loose edges (not part of a triangle)
        Set<Pair<Identifier, Identifier>> looseEdges = graph.getMap().entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(Map.Entry::getKey)
                .filter(edge -> !this.edgeInTriangle(edge, graph))
                .collect(Collectors.toSet());
        looseEdges.stream().forEach(graph::removeDistance);
    }

    /**
     * Determine if this edge forms part of a triangle in graph
     * @param edge An edge in graph
     * @param graph Graph
     * @return Boolean
     */
    private boolean edgeInTriangle(Pair<Identifier, Identifier> edge, LassoDistanceGraph graph) {
        //Determine if this edge forms part of a triangle
        Set<Identifier> leftNeighbour = graph.getNeighbours(edge.getLeft());
        leftNeighbour.retainAll(graph.getNeighbours(edge.getRight()));
        return leftNeighbour.size() > 0;
    }
}
