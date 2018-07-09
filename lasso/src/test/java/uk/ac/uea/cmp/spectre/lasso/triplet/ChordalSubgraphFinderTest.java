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

import org.junit.Test;
import uk.ac.uea.cmp.spectre.core.ds.distance.FlexibleDistanceMatrix;
import uk.ac.uea.cmp.spectre.core.ds.distance.RandomDistanceGenerator;
import uk.ac.uea.cmp.spectre.lasso.Lasso;
import uk.ac.uea.cmp.spectre.lasso.LassoDistanceGraph;

public class ChordalSubgraphFinderTest {
    @Test
    public void spanningTree() {
        double[][] matrix = {{0, 1, 1, 0}, {1, 0, 1, 0}, {1, 1, 0, 0}, {1, 0, 0, 0}};
        LassoDistanceGraph lg = new LassoDistanceGraph(new FlexibleDistanceMatrix(matrix));
        ChordalSubgraphFinder chordal = new ChordalSubgraphFinder();
        chordal.find(lg);
    }

    @Test
    public void find() {
        double[][] matrix = {{0, 1, 1, 0, 0, 0}, {1, 0, 0, 1, 0, 0}, {1, 0, 0, 1, 1, 1}, {0, 1, 1, 0, 1, 0}, {0, 0, 1, 1, 0, 1}, {0, 0, 1, 0, 1, 0}};
        LassoDistanceGraph lg = new LassoDistanceGraph(new FlexibleDistanceMatrix(matrix));
        ChordalSubgraphFinder cf = new ChordalSubgraphFinder();
//        LassoDistanceGraph chordalSub = cf.find(lg);
//        System.out.println(chordalSub);
        lg = new LassoDistanceGraph(new RandomDistanceGenerator().generateDistances(300));
        LassoDistanceGraph chordalSub = cf.find(lg);
    }
}
