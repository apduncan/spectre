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
import org.junit.Test;
import static org.junit.Assert.*;
import uk.ac.uea.cmp.spectre.core.ds.distance.FlexibleDistanceMatrix;
import uk.ac.uea.cmp.spectre.lasso.LassoDistanceGraph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TripletCoverFinderTest {

    @Test
    public void findTripletCovers() {
        //This is a triplet cover with no missing chords which are cherries. This should find all taxa, and all
        //edges defined by the matrix
        double[][] matrix = {{0, 3, 8, 0, 0}, {3, 0, 9, 16, 0}, {8, 9, 0, 15, 16}, {0, 16, 15, 0, 13}, {0, 0, 16, 13, 0}};
        LassoDistanceGraph lg = new LassoDistanceGraph(new FlexibleDistanceMatrix(matrix));
        TripletCoverFinder tc = new TripletCoverFinder(lg);
        List<LassoDistanceGraph> covers = tc.findTripletCovers();
        assertEquals(1, covers.size());
        //Test it has all taxa included
        LassoDistanceGraph cover = covers.get(0);
        assertTrue(cover.getTaxa().containsAll(lg.getTaxa()));
        //Test that the edges match those in the input
        double[][] derived = cover.getMatrix(Comparator.naturalOrder());
        int i = 0;
        for(double[] row : derived) {
            assertArrayEquals(matrix[i], row, 0.01);
            i++;
        }
    }

   @Test
   public void findTripleCover_multiple() {
        //Find triplet covers where there are two covers which cannot be joined
        final double[][] matrix = {{0, 3, 8, 0, 0, 0, 0, 0}, {3, 0, 9, 16, 0, 0, 0, 0}, {8, 9, 0, 15, 16, 0, 0, 0},
                {0, 16, 15, 0, 13, 0, 0, 0}, {0, 0, 16, 13, 0, 3, 2, 0}, {0, 0, 0, 0, 3, 0, 3, 2},
                {0, 0, 0, 0, 2, 3, 0, 3}, {0, 0, 0, 0, 0, 2, 3, 0}};
        final double[][] expectedOne = {{0, 3, 8, 0, 0}, {3, 0, 9, 16, 0}, {8, 9, 0, 15, 16}, {0, 16, 15, 0, 13},
                {0, 0, 16, 13, 0}};
        final double[][] expectedTwo = {{0, 3, 2, 0}, {3, 0, 3, 2}, {2, 3, 0, 3}, {0, 2, 3, 0}};

        LassoDistanceGraph lg = new LassoDistanceGraph(new FlexibleDistanceMatrix(matrix));
        List<LassoDistanceGraph> covers = new TripletCoverFinder(lg).findTripletCovers();
        assertEquals(2, covers.size());

        //One cover should contain 5 cords, the other 7
        long cordsA = covers.get(0).getMap().values().stream().filter(v -> v > 0).count();
        long cordsB = covers.get(1).getMap().values().stream().filter(v -> v > 0).count();
        assertTrue((cordsA == 5) ^ (cordsB == 5));
        assertTrue((cordsA == 7) ^ (cordsB == 7));
        LassoDistanceGraph small = cordsA == 5 ? covers.get(0) : covers.get(1);
        LassoDistanceGraph large = small == covers.get(0) ? covers.get(1) : covers.get(0);
        //Check the output matrix matches the expected matrix
        List<Pair<LassoDistanceGraph, double[][]>> check = new ArrayList<>();
        check.add(new ImmutablePair<>(small, expectedTwo));
        check.add(new ImmutablePair<>(large, expectedOne));
        for(Pair<LassoDistanceGraph, double[][]> pair : check) {
            double[][] derived = pair.getLeft().getMatrix(Comparator.naturalOrder());
            for(int i = 0; i < pair.getRight().length; i++) {
                assertArrayEquals(pair.getRight()[i], derived[i], 0.01);
            }
        }
   }
}