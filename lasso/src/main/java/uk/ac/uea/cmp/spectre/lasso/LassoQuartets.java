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
import uk.ac.uea.cmp.spectre.core.ds.quad.Quad;
import uk.ac.uea.cmp.spectre.core.ds.quad.SpectreQuad;
import uk.ac.uea.cmp.spectre.core.ds.quad.quartet.QuartetSystem;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class LassoQuartets {
    private LassoDistanceGraph matrix;
    //Subsets for 4c2. Used in place of combination class for speed.
    private final static int[][] PAIRS_OF_QUAD = {{0, 1}, {0, 2}, {0, 3}, {1, 2}, {1, 3}, {2, 3}};

    public LassoQuartets(DistanceMatrix matrix) {
        this.matrix = new LassoDistanceGraph(matrix);
    }

    public DistanceMatrix getMatrix() {
        return this.matrix;
    }

    /**
     * Add distances in the matrix by locating diamonds the graph of cords, where the missing distance is not between
     * two taxa forming a cherry. Continue doing so until no new cords and weights can be inferred. If the matrix is
     * not a strong lasso, there could be multiple weights which could be inferred for a cord - the first possible
     * weight encountered will be used. An alternate method, altEnrichMatrix, is available, which is faster but with
     * higher memory use.
     * @return A matrix with as many cords and weights as possible inferred.
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

    /**
     * Add distances in the matrix by locating diamonds the graph of cords, where the missing distance is not between
     * two taxa forming a cherry. Continue doing so until no new cords and weights can be inferred. If the matrix is
     * not a strong lasso, there could be multiple weights which could be inferred for a cord - the first possible
     * weight encountered will be used. A faster but more memory intensive method than enrichMatrix.
     * @return A matrix with as many cords and weights as possible inferred.
     */
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

    /**
     * Get the neighours of vertex in graph, using a cached result in cache if this had been looked up before
     * @param vertex Vertex to find neighbours of
     * @param graph The graph to find neighbours in
     * @param cache A map which holds previously looked up sets of neighbours
     * @return The neighbours of vertex in graph.
     */
    private Set<Identifier> getNeighoursCache(Identifier vertex, LassoDistanceGraph graph,
          Map<Identifier, Set<Identifier>> cache) {
        if(!cache.containsKey(vertex)) {
            Set<Identifier> neighbours = graph.getNeighbours(vertex);
            cache.put(vertex, neighbours);
            return new HashSet(neighbours);
        }
        return new HashSet(cache.get(vertex));
    }

    /**
     * Update a cached set of neigbours, such that vertices id1 and id2 are adjacent.
     * @param id1 Vertex to make adjacent to id2
     * @param id2 Vertex to make adjacent to id1
     * @param cache A map which holds previously looked up sets of neighbours
     * @param graph The graph to find id1 and id2 in
     */
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
     * Attempt to infer the length of missing cord in a quartet, and add this distance into the matrix
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

        //Check that his diamond is resolved
        //For a diamond to be resolved, the cycle (diamond less the chord) must be skew
        //For cycle {x,y,z,w}, skew if d(x,y) + d(z,w) != d(x,w) + d(y,z)
        //Either side of the inequality is a parallel edge in the diamond

        IdentifierList knownPair = new IdentifierList(combination);
        knownPair.remove(missing.getRight());
        knownPair.remove(missing.getLeft());
        double dxy = matrix.getDistance(missing.getLeft(), knownPair.get(0));
        double dzw = matrix.getDistance(missing.getRight(), knownPair.get(1));
        double dxw = matrix.getDistance(missing.getLeft(), knownPair.get(1));
        double dyz = matrix.getDistance(missing.getRight(), knownPair.get(0));

        if((dxy + dzw) == (dxw + dyz))
            return false;

        //If this is resolved, then the distance of the missing cord can be inferred
        //For missing cord yw, d(y,w) = max{d(x,y) + d(z,w), d(x,w) + d(y,z)} - d(x,y)
        double dxu = matrix.getDistance(knownPair.get(0), knownPair.get(1));
        double dyw = Math.max(dxy + dzw, dxw + dyz) - dxu;
        matrix.setDistance(missing.getRight(), missing.getLeft(), dyw);
        return true;
    }

    /**
     * Returns a stream which can generate all valid quartets from the current matrix. The quartets are not generated
     * until a terminal operation is called on the stream, so the terminal operation will be when computation occurs.
     * @return A stream which will generate all quartets
     */
    public Stream<Quad> quartetStream() {
        IdentifierCombinations combinations = new IdentifierCombinations(matrix.getTaxa(), 4);
        return StreamSupport.stream(combinations.spliterator(), false)
            .map(this::getQuartet)
            .filter(quartet -> quartet != null);
    }


    /**
     * Find and return all valid quartets in the current matrix. A valid quartet is one meeting the four point
     * condition.
     * @return All valid quartets in the matrix
     */
    public QuartetSystem getQuartets(boolean weighted) {
        //Iterate over every combination of taxa
        //Attempt to find a valid quartet and which pairs are cherries
        QuartetSystem quartets = new QuartetSystem();
        quartets.setTaxa(matrix.getTaxa());
        //TODO: Replace with collect operation which instantiates a quartet system and adds to it
        quartetStream().forEach(quartet -> {
            quartets.getQuartets().put(quartet, weighted ? getQuartetWeight(quartet) : 1);
        });
        return quartets;
    }

    /**
     * Write quartets out into nexus file. Does not write header, taxa info, this will only write the quartet block
     * @return The NexusWriter
     */
    public StringBuilder getQuartetsAsString(boolean weighted) {
        //Find quartets, and write them directly into a StringBuilder object. Avoid having to hold large quartet system
        //and large string output in memory simultaneously.
        StringBuilder writer = new StringBuilder();
        writer.append("BEGIN Quartets;\n");
        writer.append("  DIMENSIONS NTAX=" + matrix.getTaxa().size() + " NQUARTETS=");
        //need to insert number of quartets later, so store the end position
        int noPos =  writer.length();
        AtomicLong quartetsFound = new AtomicLong(0);
        writer.append("\n  FORMAT\n");
        writer.append("    LABELS=NO\n");
        writer.append("    WEIGHTS=" + (weighted ? "YES" : "NO") + "\n");
        writer.append("  ;\n");
        writer.append("  MATRIX\n");
        //Iterate over existing quartets
        quartetStream().forEach(quartet -> {
            quartetsFound.incrementAndGet();
            writer.append("    " + (weighted ? getQuartetWeight(quartet).toString() : "") + quartetToString(quartet) + "\n");
        });
        writer.append("  ;\n");
        writer.append("END; [Quartets]\n");
        //Add in the number of quartets found
        writer.insert(noPos, quartetsFound.toString() + ";\n");
        return writer;
    }

    private String quartetToString(Quad quartet) {
        String string = "0 ";
        int i = 0;
        for(int idx : quartet.toIntArray()) {
            string = string + idx + " ";
            string = string + (i == 1 ? ": ": "");
            i++;
        }
        string = string.trim() + ",";
        return string;
    }

    /**
     * Find a valid quartet from the four identifiers provided. If not a valid quartet, will return null.
     * @return A quartet if one exists, or null if no quartet
     */
    private Quad getQuartet(IdentifierList quad) {
        //Check all distances are in matrix
        for(int[] pair : LassoQuartets.PAIRS_OF_QUAD) {
            if(matrix.getDistance(quad.get(pair[0]), quad.get(pair[1])) <= 0) {
                return null;
            }
        }
        //Get four point condition distances
        double bcad = matrix.getDistance(quad.get(1), quad.get(2)) + matrix.getDistance(quad.get(0), quad.get(3));
        double abcd = matrix.getDistance(quad.get(0), quad.get(1)) + matrix.getDistance(quad.get(2), quad.get(3));
        double bdac = matrix.getDistance(quad.get(1), quad.get(3)) + matrix.getDistance(quad.get(0), quad.get(2));
        //The two which are not minimal must be equal
        double min = Math.min(bcad, Math.min(abcd, bdac));
        if(abcd == min && bcad != bdac)
            return null;
        if(bcad == min && abcd != bdac)
            return null;
        if(bdac == min && bcad != abcd)
            return null;
        //The distance which is smaller that the sum of the other two has the two cherries
        if(bcad == min)
            return new SpectreQuad(quad.get(1).getId(), quad.get(2).getId(), quad.get(0).getId(), quad.get(3).getId());
        if(abcd == min)
            return new SpectreQuad(quad.get(0).getId(), quad.get(1).getId(), quad.get(2).getId(), quad.get(3).getId());
        if(bdac == min)
            return new SpectreQuad(quad.get(1).getId(), quad.get(3).getId(), quad.get(0).getId(), quad.get(2).getId());
        return null;
    }

    /**
     * Calculate the weight of a quartet. The weight of a quartet is the length of the internal edge. Assumes that the
     * quartet fits the four point condition.
     * @param quartet Quartet to weight
     * @return Weight of the internal edge of the quartet
     */
    private Double getQuartetWeight(Quad quartet) {
        //Weight of a quartet is the weight of the internal edge
        Identifier a = matrix.getTaxa().getById(quartet.getA());
        Identifier b = matrix.getTaxa().getById(quartet.getB());
        Identifier c = matrix.getTaxa().getById(quartet.getC());
        Identifier d = matrix.getTaxa().getById(quartet.getD());
        //Quartet is ab|cd
        Double ab = matrix.getDistance(a, b);
        Double cd = matrix.getDistance(c, d);
        Double ac = matrix.getDistance(a, c);
        Double bd = matrix.getDistance(b, d);
        return ((ac+bd)-(ab+cd))/2;
    }

    /**
     * Make a set of all possible pairs of taxa in taxa
     * @param taxa Set of taxa to make pairs of
     * @return A set containing all pairs of identifiers
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
     * Determine if all of a set of edges exist in the current matrix.
     * An edge is determined as missing if the distance between the two vertices is 0.
     * @param distances A set of pairs of taxa to determine if there is a distance between
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

    /**
     * Find if an edge exists.
     * @param pair Vertices to test if there is an edge between
     * @return Boolean
     */
    private boolean distancesInMatrix(Pair<Identifier, Identifier> pair) {
        Set<Pair<Identifier, Identifier>> single = new HashSet<Pair<Identifier, Identifier>>();
        single.add(pair);
        return distancesInMatrix(single);
    }

}
