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
import uk.ac.uea.cmp.spectre.core.ds.quad.Quad;
import uk.ac.uea.cmp.spectre.core.ds.quad.SpectreQuad;
import uk.ac.uea.cmp.spectre.core.ds.quad.quartet.QuartetSystem;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LassoQuartets {
    private DistanceMatrix matrix;

    public LassoQuartets(DistanceMatrix matrix) {
        this.matrix = matrix;
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
        for(IdentifierList quad : combinations) {
            Quad quartet = getQuartet(quad);
            if(quartet != null) {
                //Add the found quartet to system. Give all weight 0 as not assigning weights.
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
        Set<Pair<Identifier, Identifier>> pairs = allPairs(quad);
        if(!distancesInMatrix(pairs))
            return null;
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
