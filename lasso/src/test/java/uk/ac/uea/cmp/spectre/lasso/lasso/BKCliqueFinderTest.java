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

package uk.ac.uea.cmp.spectre.lasso.lasso;

import org.junit.Before;
import org.junit.Test;
import uk.ac.uea.cmp.spectre.core.ds.Identifier;
import uk.ac.uea.cmp.spectre.core.ds.distance.FlexibleDistanceMatrix;
import uk.ac.uea.cmp.spectre.lasso.LassoDistanceGraph;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class BKCliqueFinderTest {
    private LassoDistanceGraph simpleGraph;
    private LassoDistanceGraph singleValidClique;

    @Before
    public void setUp() throws Exception {
        double[][] matrix = new double[][]{{0, 2, 2, 0}, {2, 0, 2, 0}, {2, 2, 0, 4}, {0, 0, 4, 0}};
        double[][] singleValid = new double[][]{{0, 2, 0}, {2, 0, 3}, {0, 3, 0}};
        this.simpleGraph = new LassoDistanceGraph(new FlexibleDistanceMatrix(matrix));
        this.singleValidClique = new LassoDistanceGraph(new FlexibleDistanceMatrix(singleValid));
    }

    @Test
    public void find() {
         CliqueFinder heuristic = CliqueFinderFactory.HEURISTIC.get(new LassoOptions());
        Set<Identifier> expected = new HashSet<>();
        for(int i = 0; i < 3; i++) {
            expected.add(this.simpleGraph.getTaxa().get(i));
        }
        Set<Identifier> clique = heuristic.find(this.simpleGraph);
        //Test that the expected taxa are in the clique, and only those expected
        assertTrue(clique.containsAll(expected) && clique.size() == expected.size());
        expected.removeIf(v -> true);
        expected.add(this.singleValidClique.getTaxa().get(0));
        expected.add(this.singleValidClique.getTaxa().get(1));
        clique = heuristic.find(this.singleValidClique);
        assertTrue(clique.containsAll(expected) && clique.size() == expected.size());
    }
}