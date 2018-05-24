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

import org.junit.Before;
import org.junit.Test;
import uk.ac.uea.cmp.spectre.core.ds.Identifier;
import uk.ac.uea.cmp.spectre.core.ds.distance.FlexibleDistanceMatrix;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ModalDistanceUpdaterTest {
    @Before
    public void setUp() throws Exception {
        //nothing
    }

    @Test
    public void update() {
        double[][] matrix = new double[][] { {0, 2, 2, 0}, {2, 0, 2, 0}, {2, 2, 0, 4}, {0, 0, 4, 0} };
        double[][] reducedMatrix = new double[][] { {0, 4}, {4, 0} };
        LassoDistanceGraph lg = new LassoDistanceGraph(new FlexibleDistanceMatrix(matrix));
        List<Identifier> cluster = new ArrayList<>();
        cluster.add(lg.getTaxa().get(0));
        cluster.add(lg.getTaxa().get(1));
        cluster.add(lg.getTaxa().get(2));
        LassoTree clustered = lg.joinCluster(cluster, DistanceUpdaterFactory.MODAL);
        //test the graph matrix equals the expected reduced matrix
        matrix = lg.getMatrix();
        for(int i = 0; i < matrix.length; i++) {
            assertArrayEquals(reducedMatrix[i], matrix[i], 0.001);
        }
    }
}