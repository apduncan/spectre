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
import uk.ac.uea.cmp.spectre.core.ds.distance.RandomDistanceGenerator;
import uk.ac.uea.cmp.spectre.core.ds.quad.Quad;
import uk.ac.uea.cmp.spectre.core.ds.quad.SpectreQuad;
import uk.ac.uea.cmp.spectre.core.ds.quad.quartet.QuartetSystem;
import uk.ac.uea.cmp.spectre.lasso.LassoDistanceGraph;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LassoQuartets {
    private LassoDistanceGraph matrix;
    private final static int[][] PAIRS_OF_QUAD = {{0, 1}, {0, 2}, {0, 3}, {1, 2}, {1, 3}, {2, 3}};

    public LassoQuartets(DistanceMatrix matrix) {
        this.matrix = new LassoDistanceGraph(matrix);
    }

    /**
     * Search for diamonds representing quarter where 5 distances are known, and one missing. If possible, infer
     * this missing distance, and add to the matrix. Repeat until no cords can be added.
     * @return
     */
    public DistanceMatrix enrichMatrix() {
        long size = 0;
        IdentifierCombinations combinations = new IdentifierCombinations(matrix.getTaxa(), 4);

        do {
            size = matrix.getMap().values().stream().filter(distance -> distance > 0).count();
            for(IdentifierList combination : combinations) {
                //Make the possible set of pairwise distances for this quarter
                Set<Pair<Identifier, Identifier>> pairSet = allPairs(combination);

                //Iterate over each distance x. If pairSet - x is in dm, and x is not in dm, then infer x.
                for(Pair pair : pairSet) {
                    Set<Pair<Identifier, Identifier>> five = new HashSet(pairSet);
                    five.remove(pair);
                    //Attempt to infer missing distance is possible. If it was possible, stop checking for diamonds
                    //in this quartet.
                    if(inferDistance(five, pair, combination))
                        break;
                }
            }
            //Continue iterating over until no new edges have been added.
            //Do not count edges with weight 0 as an edge
        } while(matrix.getMap().values().stream().filter(distance -> distance > 0).count() > size);

        return matrix;
    }

    //A method to determine if element of stream are unique by a certain property
    public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor)
    {
        Map<Object, Boolean> map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    public DistanceMatrix altEnrichMatrix() {
        //An edge between one or more candidateVertices could form part of a new diamond
        Set<Identifier> candidateVertices = new HashSet<>(matrix.getTaxa());
        long size = 0;
        Map<Identifier, Set<Identifier>> neighbourCache = new HashMap<>();
        Set<Pair<Identifier, Identifier>> distancesInferred = new HashSet<>();
        do {
            size = matrix.getMap().values().stream().filter(distance -> distance > 0).count();
            final Set<Identifier> currentCandidates = new HashSet<>(candidateVertices);
            //empty out candidate vertices
            candidateVertices.removeIf((item) -> true);
            //Scan all edges which have weight > 0 and one or more vertices in candidates
            //Make a list of pairs of edges which are known, and cords which make a diamond with that edge which are
            //unknown
            List<Pair<Pair<Identifier, Identifier>, Pair<Identifier, Identifier>>> quads = matrix.getMap().entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(e -> e.getKey())
                .filter(edge -> currentCandidates.contains(edge.getLeft()) || currentCandidates.contains(edge.getRight()))
                .map((edge) -> {
                    //Get all vertices which neighbour both ends of edge, each pair of these will make a diamond
                    //Ignore any where distance between them is known
                    Set<Identifier> neighbours = getNeighoursCache(edge.getRight(), matrix, neighbourCache);
                    neighbours.retainAll(getNeighoursCache(edge.getLeft(), matrix, neighbourCache));
                    //If at least 2, make all distinct pairs
                    if(neighbours.size() > 1) {
                        IdentifierList neighbourList = new IdentifierList();
                        neighbours.forEach(identifier -> neighbourList.add(identifier));
                        Set<Pair<Identifier, Identifier>> pairs = allPairs(neighbourList);
                        return pairs.stream()
                                .filter(pair -> matrix.getDistance(pair.getLeft(), pair.getRight()) <= 0)
                                .map(pair -> new ImmutablePair(edge, pair))
                                .collect(Collectors.toList());

                    } else {
                        return null;
                    }
                })
                .filter(list -> list != null)
                .flatMap(list -> list.stream())
                //Remove diamonds which have duplicate missing distance, only want to infer once
                //.filter(distinctByKey(pair -> pair.getRight()))
                .map(item -> (Pair<Pair<Identifier, Identifier>, Pair<Identifier, Identifier>>)item)
                .collect(Collectors.toList());

            quads.forEach(quad -> {
                //for(Pair<Pair<Identifier, Identifier>, Pair<Identifier, Identifier>> quad : quads) {
                //If this distance has already been inferred, skip
                if(!distancesInferred.contains(quad.getRight())) {
                    //Build all cords
                    IdentifierList all = new IdentifierList();
                    all.add(quad.getRight().getRight());
                    all.add(quad.getRight().getLeft());
                    all.add(quad.getLeft().getRight());
                    all.add(quad.getLeft().getLeft());
                    Set<Pair<Identifier, Identifier>> pairs = allPairs(all);
                    pairs.remove(quad.getRight());
                    //Pair could be in inverted order, also attempt to remove this
                    pairs.remove(new ImmutablePair<>(quad.getRight().getRight(), quad.getRight().getLeft()));
                    if (inferDistance(pairs, quad.getRight(), all)) {
                        //Add vertices at either end of missing distance to candidates for next iteration
                        candidateVertices.add(quad.getRight().getLeft());
                        candidateVertices.add(quad.getRight().getRight());
                        //Make these two vertices neighbours in our cached map of neighbours
                        addNeighboursCache(quad.getRight().getLeft(), quad.getRight().getRight(), neighbourCache, matrix);
                        //Mark this distance as inferred
                        distancesInferred.add(quad.getRight());
                    }
                }
            });
        } while (matrix.getMap().values().stream().filter(distance -> distance > 0).count() > size);
        return matrix;
    }

    private Set<Identifier> getNeighoursCache(Identifier vertex, LassoDistanceGraph graph,
          Map<Identifier, Set<Identifier>> cache) {
        if(!cache.containsKey(vertex)) {
            Set<Identifier> neighbours = graph.getNeighbours(vertex);
            cache.put(vertex, neighbours);
            return neighbours;
        }
        return cache.get(vertex);
    }

    private void addNeighboursCache(Identifier id1, Identifier id2, Map<Identifier, Set<Identifier>> cache, LassoDistanceGraph graph) {
        //Make id1 neighbour id2 and vice versa
        Set<Identifier> id1Neighbours = getNeighoursCache(id1, graph, cache);
        id1Neighbours.add(id2);
        Set<Identifier> id2Neighbours = getNeighoursCache(id2, graph, cache);
        id2Neighbours.add(id1);
        cache.put(id1, id1Neighbours);
        cache.put(id2, id2Neighbours);
    }

    /**
     * Attempt to infer the length of missing cord in a quarter, and add this distance into the matrix
     * @param known The five cords known in the quarter
     * @param missing The missing cord
     * @param combination The set of identifiers which make up the taxa of this quartet
     * @return True if distance was inferred
     */
    private boolean inferDistance(Set<Pair<Identifier, Identifier>> known, Pair<Identifier, Identifier> missing,
                                    IdentifierList combination) {
        //Check the five distances are known
        if(!distancesInMatrix(known))
            return false;

        //Check the one distance is unknown
        if(distancesInMatrix(missing))
            return false;

        //Check the missing distance is a cherry
        //Remove the taxa from the misisng pair from the combination
        IdentifierList knownPair = new IdentifierList(combination);
        knownPair.remove(missing.getRight());
        knownPair.remove(missing.getLeft());
        //Missing cord xz must satisfy d(x,y) + d(u,z) < d(x,u) + d(y,z)
        Double dxy = matrix.getDistance(missing.getLeft(), knownPair.get(0));
        Double duz = matrix.getDistance(missing.getRight(), knownPair.get(1));
        Double dxu = matrix.getDistance(knownPair.get(1), missing.getLeft());
        Double dyz = matrix.getDistance(knownPair.get(0), missing.getRight());
        if(dxy + duz >= dxu + dyz)
            return false;

        //This is a valid quartet, so induce distance xz and add to matrix
        Double dxz = dxu + dyz - matrix.getDistance(knownPair.get(0), knownPair.get(1));
        matrix.setDistance(missing.getLeft(), missing.getRight(), dxz);
        return true;
    }


    public QuartetSystem getQuartets() {
        //Iterate over every combination of taxa
        //Attempt to find a valid quartet and which pairs are cherries
        QuartetSystem quartets = new QuartetSystem();
        quartets.setTaxa(matrix.getTaxa());
        IdentifierCombinations combinations = new IdentifierCombinations(matrix.getTaxa(), 4);
        for(IdentifierList quad: combinations) {
            Quad quartet = getQuartet(quad);
            if(quartet != null) {
                quartets.getQuartets().put(quartet, Double.valueOf(0));
            }
        }
        return quartets;
    }

    /**
     * Find a valid quarter from the four identifiers provided. If not valid quartet, will return null.
     * @return A quartet if one exists, or null if no quartet
     */
    private Quad getQuartet(IdentifierList quad) {
        //Check all distances are in matrix
//        Set<Pair<Identifier, Identifier>> pairs = allPairs(quad);
//        if(!distancesInMatrix(pairs))
//            return null;
        for(int[] pair : LassoQuartets.PAIRS_OF_QUAD) {
            if(matrix.getDistance(quad.get(pair[0]), quad.get(pair[1])) <= 0) {
                return null;
            }
        }
        //Get four point condition distances
        double bcad = matrix.getDistance(quad.get(1), quad.get(2)) + matrix.getDistance(quad.get(0), quad.get(3));
        double abcd = matrix.getDistance(quad.get(0), quad.get(1)) + matrix.getDistance(quad.get(2), quad.get(3));
        double bdac = matrix.getDistance(quad.get(1), quad.get(3)) + matrix.getDistance(quad.get(0), quad.get(2));
        //The distance which is smaller that the sum of the other two has the two cherries
        if(bcad < Math.max(abcd, bdac))
            return new SpectreQuad(quad.get(1).getId(), quad.get(2).getId(), quad.get(0).getId(), quad.get(3).getId());
        if(abcd < Math.max(bcad, bdac))
            return new SpectreQuad(quad.get(0).getId(), quad.get(1).getId(), quad.get(2).getId(), quad.get(3).getId());
        if(bdac < Math.max(bcad, abcd))
            return new SpectreQuad(quad.get(1).getId(), quad.get(3).getId(), quad.get(0).getId(), quad.get(2).getId());
        return null;
    }

    /**
     * Make a set of all possible pairs of taxa in taxa
     * @param taxa Set of taxa to make pairs of
     * @return A set contain all pairs of identifiers
     */
    private Set<Pair<Identifier, Identifier>> allPairs(IdentifierList taxa) {
        //Make the possible set of pairwise distances for this quarter
        IdentifierCombinations pairs = new IdentifierCombinations(taxa, 2);
        //Map the combination of pairs into a set of pair of objects
        Set<Pair<Identifier, Identifier>> pairSet = new HashSet<>();
        for (IdentifierList pair : pairs) {
            pairSet.add(new ImmutablePair<>(pair.get(0), pair.get(1)));
        }
        return pairSet;
    }

    /**
     * Determine if all the distances between pairs in distances exist in the matrix.
     * A distance is determined to be missing if the distance is 0.
     * @param distances A set of pairs of taxa to determine if there are distance between
     * @return Boolean
     */
    private boolean distancesInMatrix(Set<Pair<Identifier, Identifier>> distances) {
        for(Pair<Identifier, Identifier> distance : distances) {
           if(matrix.getDistance(distance.getLeft(), distance.getRight()) <= 0) {
               return false;
           }
        }
        return true;
    }

    private boolean distancesInMatrix(Pair<Identifier, Identifier> pair) {
        Set<Pair<Identifier, Identifier>> single = new HashSet<Pair<Identifier, Identifier>>();
        single.add(pair);
        return distancesInMatrix(single);
    }

}
