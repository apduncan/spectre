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

package uk.ac.uea.cmp.spectre.lasso;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import uk.ac.uea.cmp.spectre.core.ds.Identifier;
import uk.ac.uea.cmp.spectre.core.ds.IdentifierList;
import uk.ac.uea.cmp.spectre.core.ds.distance.DistanceMatrix;
import uk.ac.uea.cmp.spectre.core.ds.distance.FlexibleDistanceMatrix;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class LassoDistanceGraph extends FlexibleDistanceMatrix {
    private Map<Identifier, LassoTree> identifierMap;
    private Map<Pair<Identifier, Identifier>, Set<Pair<Identifier, Identifier>>> distanceSupport;
    private Set<Pair<Identifier, Identifier>> distancesUsed;
    private int vertexId = -2;

    /**
     * Copy constructor - quite time consuming, avoid using if possible
     * @param copy Distance graph to copy
     */
    public LassoDistanceGraph(LassoDistanceGraph copy) {
        super(copy);
        //Copy uk.ac.uea.cmp.spectre.lasso.Lasso specific elements
        //Copy identifier - cluster mapping
        identifierMap = new HashMap<>();
        distanceSupport = new HashMap<>();
        distancesUsed = new HashSet<>();
        copy.identifierMap.entrySet().parallelStream()
                .forEach((entry) -> identifierMap.put(new Identifier(entry.getKey()), new LassoTree(entry.getValue())));
        this.removeZeroWeights();
    }

    public LassoDistanceGraph(DistanceMatrix matrix) {
        super(matrix);
        identifierMap = new HashMap<>();
        distanceSupport = new HashMap<>();
        distancesUsed = new HashSet<>();
        this.removeZeroWeights();
    }

    public LassoDistanceGraph(double[][] matrix) {
        super(matrix);
        identifierMap = new HashMap<>();
        distanceSupport = new HashMap<>();
        distancesUsed = new HashSet<>();
        this.removeZeroWeights();
    }

    public Set<Pair<Identifier, Identifier>> getDistancesUsed() {
        return this.distancesUsed;
    }

    /**
     * Remove any edges inthe map with weight 0
     */
    private void removeZeroWeights() {
        Set<Pair<Identifier, Identifier>> remove = this.getMap().entrySet().stream()
                .filter(entry -> entry.getValue() == 0)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        remove.stream().forEach(this::removeDistance);
    }

    /**
     * Get any vertices which are adjacent to vertex. Distances of 0 are treated as no edge existing.
     *
     * @param vertexIn Vertex to get neighbours of
     * @param weight   Return neighbours which are connected by edges with only this weighting
     * @return Set of neighbouring vertices
     */
    public Set<Identifier> getNeighbours(final Identifier vertexIn, final Double weight) {
        final Identifier vertex = this.getLocalIdentifier(vertexIn);
        return this.getTaxa().parallelStream()
                .filter(v -> {
                    final double distance = this.getDistance(v, vertex);
                    return weight == null ? distance > 0 : distance == weight;
                })
                .collect(Collectors.toSet());
    }

    /**
     * Get any vertices which are adjacent to vertex. Distance of 0 are treated as no edge existing.
     *
     * @param vertexIn Vertex to get neighbours of
     * @return Set of neighbouring vertices
     */
    public Set<Identifier> getNeighbours(final Identifier vertexIn) {
        return this.getNeighbours(vertexIn, null);
    }

    /**
     * Delete a distance between two vertices. Order of start and end does not matter.
     *
     * @param start Start vertex
     * @param end   End vertex
     */
    public void removeDistance(Identifier start, Identifier end) {
        List<Pair<Identifier, Identifier>> remove = this.getMap().entrySet().parallelStream()
                .map(entry -> entry.getKey())
                //Filter to only those where ends are equal to the identifiers provided
                .filter(pair -> {
                    return (pair.getLeft().equals(start) && pair.getRight().equals(end)) ||
                            (pair.getLeft().equals(end) && pair.getRight().equals(start));
                })
                .collect(Collectors.toList());
        //Remove each identified pair from the map
        remove.parallelStream().forEach(pair -> this.removeDistance(pair));
    }

    @Override
    public void removeTaxon(Identifier identifier) {
        super.removeTaxon(identifier);
        //Remove from identifier map
        if (identifierMap.containsKey(identifier)) {
            identifierMap.remove(identifier);
        }
    }

    /**
     * Delete a distance between two vertices. Deletes by reference equality, must be the Pair object from the
     * underlying map
     *
     * @param pair Vertices the distance is between
     */
    public void removeDistance(Pair<Identifier, Identifier> pair) {
        this.getMap().remove(pair);
    }

    /**
     * Deletes any edges which do not have the minimum edge weight in the graph.
     *
     * @return The minimum edge weight found
     */
    public double retainMinEdges() {
        double min = this.getMinEdgeWeight();
        List<Pair<Identifier, Identifier>> remove = this.getMap().entrySet().parallelStream()
                .filter(entry -> entry.getValue() > min)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        //Remove identified edges
        remove.stream().forEach(this::removeDistance);
        return min;
    }

    /**
     * Find the minimum edge weight in the graph
     *
     * @return The minimum edge weight
     */
    public double getMinEdgeWeight() {
        //Identify the minimum edge weight
        Optional<Double> min = this.getMap().entrySet().parallelStream()
                .map(entry -> Double.valueOf(entry.getValue()))
                .filter(weight -> weight > 0)
                .min(Double::compareTo);
        if (!min.isPresent())
            throw new IllegalStateException("No edges in graph");
        return min.get();
    }

    /**
     * Find an Identifier matching the properties in vertex, or throw an error if one does not exists
     *
     * @param vertex Identifier which may have come from a copy of this graph
     * @return The matching identifier which this instance is using
     */
    private Identifier getLocalIdentifier(Identifier vertex) {
        if (!this.getTaxa().contains(vertex)) {
            //Try finding by equality function rather than reference
            Optional<Identifier> localVertex = this.getTaxa().stream()
                    .filter(identifier -> identifier.equals(vertex))
                    .findFirst();
            if (!localVertex.isPresent())
                throw new IllegalStateException("Identifier not present in graph");
            return localVertex.get();
        } else {
            return this.getTaxa().getByName(vertex.getName());
        }
    }

    /**
     * Get the cluster which vertex is the parent of. If vertex is a taxon, cluster will contain only that taxon.
     *
     * @param vertex Vertex which is the parent of the cluster
     * @return A tree structure representing this cluster
     */
    public LassoTree getCluster(Identifier vertex) {
        Identifier local = this.getLocalIdentifier(vertex);
        if (!this.isTaxon(vertex))
            return this.identifierMap.get(local);
        else
            return new LassoTree(local);

    }

    /**
     * Is the vertex one of the leaf taxa, or representing a cluster
     *
     * @param vertex Vertex in graph
     * @return True if this is one of the leaf taxa
     */
    public boolean isTaxon(Identifier vertex) {
        Identifier local = this.getLocalIdentifier(vertex);
        return !this.identifierMap.containsKey(local);
    }

    /**
     * Perform distance matrix updates with default settings
     *
     * @param cluster The vertices to be joined
     * @param updater Matrix updater object
     * @return The joined cluster
     */
    public LassoTree joinCluster(List<Identifier> cluster, DistanceUpdater updater) {
        //Create a new vertex as parent for cluster items
        Identifier parentId = new Identifier(this.vertexId);
        parentId.setName("internal" + Double.toString(vertexId));
        this.vertexId--;
        LassoTree parent = new LassoTree(parentId);
        //Get root height of parent - distance between any two points in set / 2
        final double parentHeight = this.getDistance(cluster.get(0), cluster.get(1)) / 2;
        //Map each vertex in cluster to it's corresponding cluster, set branch length, add to parent
        for (Identifier identifier : cluster) {
            LassoTree child = getCluster(identifier);
            child.setLength(parentHeight - child.getRootHeight());
            parent.addBranch(child);
            //Work out which distances weres used for each pair being joined in the cluster
            cluster.stream().filter(v -> !v.equals(identifier)).forEach(v -> this.distanceUsed(v, identifier));
        }
        //Add parent to graph
        this.addIdentifier(parentId);
        this.identifierMap.put(parentId, parent);
        //Update distances
        Map<Identifier, Set<Identifier>> retained = updater.update(this, parentId);
        //Update mapping of which cords support the cluster - leaf distances in graph
        retained.entrySet().stream()
                .forEach(entry -> {
                    Pair<Identifier, Identifier> newPair = this.getSortedPair(parentId, entry.getKey());
                    this.distanceSupport.put(newPair, new HashSet<>());
                    entry.getValue().stream().forEach(clusterVertex -> {
                        Pair<Identifier, Identifier> pair = this.getSortedPair(clusterVertex, entry.getKey());
                        Set<Pair<Identifier, Identifier>> supp = this.distanceSupport.get(pair);
                        this.distanceSupport.remove(pair);
                        //add the previously supporting distances
                        if (supp != null)
                            this.distanceSupport.get(newPair).addAll(supp);
                        if (clusterVertex.getId() >= 0 && entry.getKey().getId() >= 0)
                            this.distanceSupport.get(newPair).add(this.getSortedPair(clusterVertex, entry.getKey()));
                    });
                });
        return parent;
    }

    protected void distanceUsed(Identifier vertex1, Identifier vertex2) {
        Pair<Identifier, Identifier> pair = this.getSortedPair(vertex1, vertex2);
        if (this.isTaxon(vertex1) && this.isTaxon(vertex2)) {
            //Both are, so this is one of the original cords
            this.distancesUsed.add(pair);
        } else if ((!this.isTaxon(vertex1) || (!this.isTaxon(vertex2)))) {
            //Both are clusters, all the distances between them are now used
            Set<Pair<Identifier, Identifier>> all = this.distanceSupport.get(pair);
            if (all != null)
                this.distancesUsed.addAll(this.distanceSupport.get(pair));
//            this.distanceSupport.remove(pair);
        }
    }

    /**
     * Return the largest cluster which has been created so far
     */
    public LassoTree getLargestCluster() {
        return this.identifierMap.values().stream()
                .reduce(null, (a, b) -> {
                    if (a == null) {
                        return b;
                    } else if (b.getNbTaxa() > a.getNbTaxa()) {
                        return b;
                    } else if (b.getNbTaxa() == a.getNbTaxa()) {
                        return new Random().nextBoolean() ? a : b;
                    } else {
                        return a;
                    }
                });
    }

    public List<LassoTree> getAllClusters() {
        return new ArrayList<>(this.identifierMap.values());
    }

    public void addIdentifierWithoutDistances(Identifier taxon) {
        if (!this.getTaxa().contains(taxon)) {
            this.getTaxa().add(taxon);
        }
    }

    /**
     * Find all connected components in the graph, and return each one in a list
     *
     * @return A list of the connected components in the original graph
     */
    public List<LassoDistanceGraph> getConnectedComponents() {
        //This private class is passed to the depthFirstSearch implementation, and operates on each vertex found
        class ComponentAccumulator implements Consumer<Identifier> {
            private LassoDistanceGraph source;
            private LassoDistanceGraph component;

            public ComponentAccumulator(LassoDistanceGraph source) {
                this.source = source;
                //blank distance matrix
                this.component = new LassoDistanceGraph(new FlexibleDistanceMatrix());
            }

            @Override
            public void accept(Identifier identifier) {
                //Add identifier
                component.addIdentifier(identifier);
                //Add all edges, and required identifiers
                for (Identifier neighbour : source.getNeighbours(identifier)) {
                    component.addIdentifierWithoutDistances(neighbour);
                    component.setDistance(identifier, neighbour, source.getDistance(identifier, neighbour));
                }
            }

            public LassoDistanceGraph getComponent() {
                return component;
            }
        }

        IdentifierList startCandidates = new IdentifierList(this.getTaxa());
        List<LassoDistanceGraph> components = new ArrayList<>();
        while (startCandidates.size() > 0) {
            ComponentAccumulator component = new ComponentAccumulator(this);
            List<Identifier> found = depthFirstSearch(startCandidates.get(0), new ArrayList<>(), component, null);
            //Remove found vertices from start candidates
            startCandidates.removeAll(found);
            components.add(component.getComponent());
        }
        return components;
    }

    /**
     * Search for all vertices reachable from vertex, and returns a list of all the discovered vertices
     * @param vertex      The vertex to visit
     * @param visited     All vertices which have been visited
     * @param vertexVisit A consumer object which should be applied to each visited vertex. Optional.
     * @return Vertices reachable from vertex
     */
    public List<Identifier> depthFirstSearch(Identifier vertex, List<Identifier> visited, Consumer<Identifier> vertexVisit,
            Consumer<Pair<Identifier, Identifier>> edgeVisit) {
        visited.add(vertex);
        if (vertexVisit != null)
            vertexVisit.accept(vertex);
        for (Identifier neighbour : this.getNeighbours(vertex)) {
            if (!visited.contains(neighbour)) {
                if(edgeVisit != null)
                    edgeVisit.accept(new ImmutablePair<>(vertex, neighbour));
                depthFirstSearch(neighbour, visited, vertexVisit, edgeVisit);

            }
        }
        return visited;
    }

    /**
     * Search for all vertices reachable from vertex, and returns a list of all the discovered vertices
     *
     * @param vertex The vertex to visit
     * @return Vertices reachable from vertex
     */
    public List<Identifier> depthFirstSearch(Identifier vertex) {
        return depthFirstSearch(vertex, new ArrayList<Identifier>(), null, null);
    }

    /**
     * Search for all vertices reachable from vertex, and returns a list of all the discovered vertices
     *
     * @param vertex The vertex to visit
     * @param visit  A consumer object which should be applied to each visited vertex
     * @return Vertices reachable from vertex
     */
    public List<Identifier> depthFirstSearch(Identifier vertex, Consumer<Identifier> visit) {
        return depthFirstSearch(vertex, new ArrayList<Identifier>(), visit, null);
    }

    /**
     * Search for all vertices reachable from vertex, and returns a list of all the discovered vertices
     *
     * @param vertex      The vertex to visit
     * @param vertexVisit A consumer object which should be applied to each visited vertex. Optional.
     * @param edgeVisit   A consumer object which should be applied to each edge traversed. Optional.
     * @return Vertices reachable from vertex
     */
    public List<Identifier> breadthFirstSearch(Identifier vertex, Consumer<Identifier> vertexVisit,
                                               Consumer<Pair<Identifier, Identifier>> edgeVisit,
                                               Set<Identifier> restrict) {
        if (restrict == null) {
            restrict = new HashSet<>(this.getTaxa());
        }
        Queue<Identifier> queue = new LinkedList<>();
        List<Identifier> visited = new ArrayList<>();
        queue.add(vertex);
        visited.add(vertex);
        while (queue.size() > 0) {
            Identifier v = queue.poll();
            if (vertexVisit != null)
                vertexVisit.accept(v);
            for (Identifier neighbour : this.getNeighbours(v)) {
                if (!visited.contains(neighbour) && restrict.contains(neighbour)) {
                    queue.add(neighbour);
                    visited.add(neighbour);
                    if (edgeVisit != null)
                        edgeVisit.accept(new ImmutablePair<>(v, neighbour));
                }
            }
        }
        return visited;
    }

    //Convenience overloads for breadth first search

    /**
     * Search for all vertices reachable from vertex
     * @param vertex Vertex to start from
     * @return A list of discovered vertices
     */
    public List<Identifier> breadthFirstSearch(Identifier vertex) {
        return breadthFirstSearch(vertex, null, null, null);
    }

    /**
     * Search for all vertices reachable from vertex, in a subgraph restricted to vertices in the set restrict.
     * @param vertex Vertex to start from
     * @param restrict A set of vertices to restrict the graph to
     * @return A set of all vertices reached
     */
    public List<Identifier> breadthFirstSearch(Identifier vertex, Set<Identifier> restrict) {
        return breadthFirstSearch(vertex, null, null, restrict);
    }

    /**
     * Search for all vertices reachable from vertex, and apply visitor objects to them
     * @param vertex Vertex to start from
     * @param vertexVisit A consumer to apply to each vertex reached
     * @param edgeVisit A consumer to apply to each edge traversed. Can be used to build a tree.
     * @return A set of all vertices reached
     */
    public List<Identifier> breadthFirstSearch(Identifier vertex, Consumer<Identifier> vertexVisit,
                                               Consumer<Pair<Identifier, Identifier>> edgeVisit) {
        return breadthFirstSearch(vertex, vertexVisit, edgeVisit, null);

    }
}
