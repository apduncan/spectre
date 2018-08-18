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

package uk.ac.uea.cmp.spectre.lasso.rooted;
import org.apache.commons.lang3.tuple.Pair;
import uk.ac.uea.cmp.spectre.core.ds.Identifier;
import uk.ac.uea.cmp.spectre.core.ds.distance.FlexibleDistanceMatrix;
import uk.ac.uea.cmp.spectre.lasso.LassoDistanceGraph;

import java.util.*;
import java.util.stream.Collectors;

public class HeuristicCliqueFinder extends CliqueFinder {
    private LassoDistanceGraph graph;
    private Map<Identifier, Set<Identifier>> neighbours;
    private Set<Identifier> visited;
    private double minWeight;
    private Map<Pair<Identifier, Identifier>, Double> distancesUsed;

    public HeuristicCliqueFinder(RootedLassoOptions options) {
        super(options);
        this.neighbours = new HashMap<>();
        this.visited = new HashSet<>();
    }

    /**
     * Find maximal clique starting with an arbitrary edge in the graph, and attempting to add vertices.
     * uk.ac.uea.cmp.spectre.rooted.rooted.RootedLassoOptions defines a number of times to attempt growing a clique.
     * Largest clique located is returned, ties broken randomly.
     * @param graph Graph to search in. For uk.ac.uea.cmp.spectre.rooted.rooted.RootedLasso, delete all non-minimal edgeweights first.
     * @return Largest located clique
     */
    @Override
    public Set<Identifier> find(LassoDistanceGraph graph) {
        if(graph.getMap().size() < 1)
            //No edges, so cannot be any cliques
            return new HashSet<Identifier>();
        this.graph = graph;
        this.neighbours = new HashMap<>();
        this.minWeight = this.graph.getMinEdgeWeight();
        List<Set<Identifier>> found = new ArrayList<>();
        for(int i = 0; i < this.getOptions().getCliqueAttempts(); i++) {
            //Get random start
            Pair<Identifier, Identifier> edge = this.randomEdge();
            //Intitialise set of vertices in clique to vertices in edge
            Set<Identifier> clique = new HashSet<>();
            clique.add(edge.getLeft());
            clique.add(edge.getRight());
            //Initialise candidates to all connected vertices, except those already in clique
            Set<Identifier> candidates = this.connectedVertices(edge);
            candidates.removeAll(clique);
            //Keep only candidates which are neighbours of the two intial vertices
            candidates.retainAll(this.getNeighbours(edge.getLeft()));
            candidates.retainAll(this.getNeighbours(edge.getRight()));
            //Iterate through all vertices and add first candidate which is adjacent to all clique members
            //Quit when no candidate found
            Identifier add = null;
            while(candidates.size() > 0) {
                //Set add to null for this iteration
                add = null;
                //Select a random vertex from candidates
                int randomIdx = new Random().nextInt(candidates.size());
                add = Identifier.class.cast(candidates.toArray()[randomIdx]);
                if (add != null) {
                    //add to clique, remove from candidates
                    clique.add(add);
                    candidates.remove(add);
                    //reduce candidates to only those which are adjacent to newly added vertex
                    candidates.retainAll(this.getNeighbours(add));
                }
            }
            //If larger than current largest clique, start list again with this
            //If equal, add to list
            //Else discard
            if(found.size() == 0 || clique.size() > found.get(0).size()) {
                found = new ArrayList<>();
                found.add(clique);
            } else if (clique.size() == found.get(0).size()) {
                found.add(clique);
            }
        }
        //Select a random clique from the largest cliques stored in found
        return found.get(new Random().nextInt(found.size()));
    }

    /**
     * Return a random edge the from the current graph.
     * @param exclude A set of edges which should not be picked
     * @return A random edge.
     */
    private Pair<Identifier, Identifier> randomEdge(Set<Pair<Identifier, Identifier>> exclude) {
        //Remove any 0 weight edges from the map
        Map<Pair<Identifier, Identifier>, Double> realEdges = this.graph.getMap().entrySet().stream()
                .filter(entry -> entry.getValue() == this.minWeight)
                .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
        Set<Pair<Identifier, Identifier>> keys = realEdges.keySet();
        keys.removeAll(exclude);
        Object[] keysArr = keys.toArray();
        return (Pair<Identifier, Identifier>)keysArr[new Random().nextInt(keysArr.length)];
    }

    private Pair<Identifier, Identifier> randomEdge() {
        //Do not exclude any edges from selection
        return this.randomEdge(new HashSet<Pair<Identifier, Identifier>>());
    }

    /**
     * Return the neighbours of a given vertex. Stores results in map to avoid iterating over distance list when
     * not needed.
     * @param vertex
     * @return Neighbouring vertices
     */
    private Set<Identifier> getNeighbours(Identifier vertex) {
        if(this.neighbours.containsKey(vertex)) {
            return new HashSet<>(this.neighbours.get(vertex));
        } else {
            Set<Identifier> neighbours = this.graph.getNeighbours(vertex, this.minWeight);
            this.neighbours.put(vertex, neighbours);
            return new HashSet<>(neighbours);
        }
    }

    /**
     * Find all vertices in the component of the graph
     * @param edge Starting edge
     * @return All vertices connected t
     */
    private Set<Identifier> connectedVertices(Pair<Identifier, Identifier> edge) {
        this.visited = new HashSet<>();
        this.traverse(edge.getLeft());
        //Do not need to traverse right, given it is connected to left.
        //Return a copy of the visited vertices, so other traversals do not alter this result set.
        return new HashSet<>(this.visited);
    }

    /**
     * Visit all vertices connected to this vertex and add them to the set of connected vertices
     * @param vertex Vertex to visit
     */
    private void traverse(Identifier vertex) {
        this.visited.add(vertex);
        Set<Identifier> neighbours = this.getNeighbours(vertex);
        //Only visited neighbours which have not already been visited
        neighbours.removeAll(this.visited);
        neighbours.stream().forEach(this::traverse);
    }

    public static void main(String[] args) {
        //For testing private methods
        double[][] matrix = new double[][] { {0, 2, 2, 0, 0}, {2, 0, 2, 0, 0}, {2, 2, 0, 0, 0}, {0, 0, 0, 0, 4}, {0, 0, 0, 4, 0}};
        LassoDistanceGraph simpleGraph = new LassoDistanceGraph(new FlexibleDistanceMatrix(matrix));
        HeuristicCliqueFinder h = new HeuristicCliqueFinder(new RootedLassoOptions());
        h.graph = simpleGraph;
        Set<Identifier> connected = h.connectedVertices((Pair<Identifier, Identifier>)simpleGraph.getMap().keySet().toArray()[3]);
        System.out.println(connected);
    }
}
