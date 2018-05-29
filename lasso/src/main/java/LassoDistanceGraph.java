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

import org.apache.commons.lang3.tuple.Pair;
import uk.ac.uea.cmp.spectre.core.ds.Identifier;
import uk.ac.uea.cmp.spectre.core.ds.distance.DistanceMatrix;
import uk.ac.uea.cmp.spectre.core.ds.distance.FlexibleDistanceMatrix;

import java.util.*;
import java.util.stream.Collectors;

public class LassoDistanceGraph extends FlexibleDistanceMatrix {
    private Map<Identifier, LassoTree> identifierMap;
    private int vertexId = -1;

    /**
     * Copy constructor - quite time consuming, avoid using if possible
     * @param copy Distance graph to copy
     */
    public LassoDistanceGraph(LassoDistanceGraph copy) {
        super(copy);
        //Copy Lasso specific elements
        //Copy identifier - cluster mapping
        identifierMap = new HashMap<>();
        copy.identifierMap.entrySet().parallelStream()
                .forEach((entry) -> identifierMap.put(new Identifier(entry.getKey()), new LassoTree(entry.getValue())));
        this.removeZeroWeights();
    }

    public LassoDistanceGraph(DistanceMatrix matrix) {
        super(matrix);
        identifierMap = new HashMap<>();
        this.removeZeroWeights();
    }

    public LassoDistanceGraph(double[][] matrix) {
        super(matrix);
        this.removeZeroWeights();
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
     * @param vertexIn Vertex to get neighbours of
     * @param weight Return neighbours which are connected by edges with only this weighting
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
     * @param vertexIn Vertex to get neighbours of
     * @return Set of neighbouring vertices
     */
    public Set<Identifier> getNeighbours(final Identifier vertexIn) {
        return this.getNeighbours(vertexIn, null);
    }

    /**
     * Delete a distance between two vertices. Order of start and end does not matter.
     * @param start Start vertex
     * @param end End vertex
     */
    public void removeDistance(Identifier start, Identifier end) {
        List<Pair<Identifier, Identifier>> remove = this.getMap().entrySet().parallelStream()
                .map(entry -> entry.getKey())
                //Filter to only those where ends are equal to the identifiers provided
                .filter(pair -> {
                    return  (pair.getLeft().equals(start) && pair.getRight().equals(end)) ||
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
        if(identifierMap.containsKey(identifier)) {
            identifierMap.remove(identifier);
        }
    }

    /**
     * Delete a distance between two vertices. Deletes by reference equality, must be the Pair object from the
     * underlying map
     * @param pair Vertices the distance is between
     */
    public void removeDistance(Pair<Identifier, Identifier> pair) {
        this.getMap().remove(pair);
    }

    /**
     * Deletes any edges which do not have the minimum edge weight in the graph.
     * @return The minimum edge weight found
     */
    public double retainMinEdges() {
        double min = this.getMinEdgeWeight();
        List<Pair<Identifier, Identifier>> remove = this.getMap().entrySet().parallelStream()
                .filter(entry -> entry.getValue() > min)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        //Remove identified edges
        remove.parallelStream().forEach(this::removeDistance);
        return min;
    }

    /**
     * Find the minimum edge weight in the graph
     * @return The minimum edge weight
     */
    public double getMinEdgeWeight() {
        //Identify the minimum edge weight
        Optional<Double> min = this.getMap().entrySet().parallelStream()
                .map(entry -> Double.valueOf(entry.getValue()))
                .filter(weight -> weight > 0)
                .min(Double::compareTo);
        if(!min.isPresent())
            throw new IllegalStateException("No edges in graph");
        return min.get();
    }

    /**
     * Find an Identifier matching the properties in vertex, or throw an error if one does not exists
     * @param vertex Identifier which may have come from a copy of this graph
     * @return The matching identifier which this instance is using
     */
    private Identifier getLocalIdentifier(Identifier vertex) {
        if(!this.getTaxa().contains(vertex)) {
            //Try finding by equality function rather than reference
            Optional<Identifier> localVertex = this.getTaxa().stream()
                    .filter(identifier -> identifier.equals(vertex))
                    .findFirst();
            if(!localVertex.isPresent())
                throw new IllegalStateException("Identifier not present in graph");
            return localVertex.get();
        } else {
            return this.getTaxa().getByName(vertex.getName());
        }
    }

    /**
     * Get the cluster which vertex is the parent of. If vertex is a taxon, cluster will contain only that taxon.
     * @param vertex Vertex which is the parent of the cluster
     * @return A tree structure representing this cluster
     */
    public LassoTree getCluster(Identifier vertex) {
        Identifier local = this.getLocalIdentifier(vertex);
        if(!this.isTaxon(vertex))
            return this.identifierMap.get(local);
        else
            return new LassoTree(local);

    }

    /**
     * Is the vertex one of the leaf taxa, or representing a cluster
     * @param vertex Vertex in graph
     * @return True if this is one of the leaf taxa
     */
    public boolean isTaxon(Identifier vertex) {
        Identifier local = this.getLocalIdentifier(vertex);
        return !this.identifierMap.containsKey(local);
    }

    /**
     * Perform distance matrix updates with default settings
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
        }
        //Add parent to graph
        this.addIdentifier(parentId);
        this.identifierMap.put(parentId, parent);
        //Update distances
        updater.update(this, parentId);
        return parent;
    }

    /**
     * Return the largest cluster which has been created so far
     */
    public LassoTree getLargestCluster() {
        return this.identifierMap.values().stream()
                .reduce(null, (a,b) -> {
                    if(a == null) { return b; }
                    else if(b.getNbTaxa() > a.getNbTaxa()) { return b; }
                    else if(b.getNbTaxa() == a.getNbTaxa()) {return new Random().nextBoolean() ? a : b; }
                    else { return a; }
                });
    }
}
