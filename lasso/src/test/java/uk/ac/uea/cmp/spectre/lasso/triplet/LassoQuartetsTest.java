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

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import uk.ac.uea.cmp.spectre.core.ds.Identifier;
import uk.ac.uea.cmp.spectre.core.ds.distance.DistanceMatrix;
import uk.ac.uea.cmp.spectre.core.ds.distance.FlexibleDistanceMatrix;
import uk.ac.uea.cmp.spectre.core.ds.distance.RandomDistanceGenerator;
import uk.ac.uea.cmp.spectre.core.ds.quad.quartet.QuartetSystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class LassoQuartetsTest {

    @Test
    public void enrichMatrix() {
        double[][] matrix = { {0, 3, 8, 0}, {3, 0, 9, 10}, {8, 9, 0, 9}, {0, 10, 9, 0} };
        DistanceMatrix dm = new FlexibleDistanceMatrix(matrix);
        LassoQuartets lq = new LassoQuartets(dm);
        dm = lq.enrichMatrix();
        //distance between a and d should be set to 9
        assertEquals(dm.getDistance("A", "D"), 9, 0.01);

        //Test if it can convert a system of 3 triangles on 5 taxa to a complete graph
        //Has 3 missing distance rather than 1
        double[][] pentMatrix = { {0, 3, 8, 0, 0}, {3, 0, 9, 16, 0}, {8, 9, 0, 15, 16}, {0, 16, 15, 0, 13}, {0, 0, 16, 13, 0} };
        DistanceMatrix pentDm = new FlexibleDistanceMatrix(pentMatrix);
        lq = new LassoQuartets(pentDm);
        pentDm = lq.enrichMatrix();
        assertEquals(pentDm.getDistance("A", "D"), 15,0.01);
        assertEquals(pentDm.getDistance("A", "E"), 16, 0.01);
        assertEquals(pentDm.getDistance("B", "E"), 17, 0.01);
    }

    @Test
    public void getQuartets() {
        //Test if it can convert a system of 3 triangles on 5 taxa to a complete graph
        //Has 3 missing distance rather than 1
        double[][] pentMatrix = { {0, 3, 8, 0, 0}, {3, 0, 9, 16, 0}, {8, 9, 0, 15, 16}, {0, 16, 15, 0, 13}, {0, 0, 16, 13, 0} };
        DistanceMatrix pentDm = new FlexibleDistanceMatrix(pentMatrix);
        LassoQuartets lq = new LassoQuartets(pentDm);
        lq.enrichMatrix();
        QuartetSystem quartets = lq.getQuartets();
        System.out.println(quartets);
    }

    @Test
    public void randomDeletions() {
        DistanceMatrix dm = new RandomDistanceGenerator().generateDistances(300);
        List<Pair<Identifier, Identifier>> pairs = new ArrayList<>(dm.getMap().keySet());
        Collections.shuffle(pairs);
        //delete 100
        for(int i = 0; i < 100; i++) {
            pairs.remove(0);
        }
        LassoQuartets lq = new LassoQuartets(dm);
        lq.enrichMatrix();
        QuartetSystem quartets = lq.getQuartets();
        System.out.println(quartets);
    }

}