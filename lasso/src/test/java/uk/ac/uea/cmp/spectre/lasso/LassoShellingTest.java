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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.Before;
import org.junit.Test;
import uk.ac.uea.cmp.spectre.core.ds.Identifier;
import uk.ac.uea.cmp.spectre.core.ds.distance.DistanceMatrix;
import uk.ac.uea.cmp.spectre.core.ds.distance.FlexibleDistanceMatrix;
import uk.ac.uea.cmp.spectre.core.ds.distance.RandomDistanceGenerator;
import uk.ac.uea.cmp.spectre.core.ds.quad.quartet.QuartetSystem;
import uk.ac.uea.cmp.spectre.core.io.nexus.Nexus;
import uk.ac.uea.cmp.spectre.core.io.nexus.NexusReader;
import uk.ac.uea.cmp.spectre.core.io.nexus.NexusWriter;
import uk.ac.uea.cmp.spectre.lasso.rooted.RootedLassoTest;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

public class LassoShellingTest {
    @Before
    public void setup() {
        BasicConfigurator.configure();
        LogManager.getRootLogger().setLevel(Level.FATAL);
    }

    @Test
    public void altEnrichMatrix() {
        //Test ability to infer correctly in an incomplete matrix, which does contain diamonds
        double[][] matrix = { {0, 3, 8, 0}, {3, 0, 9, 10}, {8, 9, 0, 9}, {0, 10, 9, 0} };
        DistanceMatrix dm = new FlexibleDistanceMatrix(matrix);
        LassoShelling lq = new LassoShelling(dm);
        dm = lq.altShell();
        //distance between a and d should be set to 9, can be inferred from diamond
        assertEquals(dm.getDistance("A", "D"), 9, 0.01);

        //Test if it can convert a system of 3 triangles on 5 taxa to a complete graph
        //Has 3 missing distance rather than 1
        double[][] pentMatrix = { {0, 3, 8, 0, 0}, {3, 0, 9, 16, 0}, {8, 9, 0, 15, 16}, {0, 16, 15, 0, 13},
                {0, 0, 16, 13, 0} };
        DistanceMatrix pentDm = new FlexibleDistanceMatrix(pentMatrix);
        lq = new LassoShelling(pentDm);
        //Check missing distances correctly inferrred
        pentDm = lq.altShell();
        assertEquals(pentDm.getDistance("A", "D"), 15,0.01);
        assertEquals(pentDm.getDistance("A", "E"), 16, 0.01);
        assertEquals(pentDm.getDistance("B", "E"), 17, 0.01);
    }

    //@Test
    public void timeComparison() {
        //Used to compare runtime of alt and standard enrich methods
        DistanceMatrix dm = new RandomDistanceGenerator().generateDistances(40);
        List<Pair<Identifier, Identifier>> pairs = new ArrayList<>(dm.getMap().keySet());
        Collections.shuffle(pairs);
        //delete 20
        System.out.println("Deleting edges");
        long delete = Math.round(((dm.getTaxa().size() * (dm.getTaxa().size() - 1))/2) * 0.15);
        for(int i = 0; i < delete; i++) {
            dm.setDistance(pairs.get(i).getLeft(), pairs.get(i).getRight(), 0);
        }
        LassoShelling lq = new LassoShelling(dm);
        StopWatch timer = new StopWatch();
        System.out.println("Begin standard enriching");
        timer.start();
        lq.shell();
        System.out.println("Finished enriching standard");
        timer.stop();
        System.out.println("Time: " + timer.getTime());
        timer.reset();
        lq = new LassoShelling(dm);
        timer.start();
        lq.altShell();
        timer.stop();
        System.out.println("Finished enriching alternate");
        System.out.println("Time: " + timer.getTime());

        QuartetSystem quartets = lq.getQuartets(true);
        StringBuilder sb = lq.getQuartetsAsString(true);
        NexusWriter writer = new NexusWriter();
        writer.appendHeader();
        writer.appendLine();
        writer.append(quartets.getTaxa());
        writer.append(lq.getMatrix().getTaxa());
        writer.appendLine();
        writer.append(quartets, false);
        writer.append(sb.toString());
        try {
            writer.write(new File("/home/hal/test-quartet.nex"));
        } catch (IOException e) {
            System.out.println("Write err: "  + e.toString());
        }
    }

//    @Test
    public void yeastTest() {
        File input = FileUtils.toFile(RootedLassoTest.class.getResource("/paradoxus-part-question.nex"));
        File output = new File("/home/hal/quartet-yeast.nex");
        try {
            Nexus nexus = new NexusReader().parse(input);
            LassoDistanceGraph lg = new LassoDistanceGraph(new FlexibleDistanceMatrix(nexus.getDistanceMatrix()));
            LassoDistanceGraph original = new LassoDistanceGraph(lg);
            LassoShelling lq = new LassoShelling(lg);
            FlexibleDistanceMatrix dm = new FlexibleDistanceMatrix(lq.altShell());
            QuartetSystem quartets = lq.getQuartets(true);
            //try to output
            NexusWriter writer = new NexusWriter();
            writer.appendHeader();
            writer.appendLine();
            writer.append(quartets.getTaxa());
            writer.appendLine();
            writer.append(quartets, true);
            writer.write(output);
        } catch (IOException e) {
            System.out.println("Error reading nexus file");
        }
    }

    @Test
    public void getQuartets() {
        //Test if it can convert a system of 3 triangles on 5 taxa to a complete graph
        //Has 3 missing distance rather than 1
        double[][] pentMatrix = { {0, 3, 8, 0, 0}, {3, 0, 9, 16, 0}, {8, 9, 0, 15, 16}, {0, 16, 15, 0, 13}, {0, 0, 16, 13, 0} };
        DistanceMatrix pentDm = new FlexibleDistanceMatrix(pentMatrix);
        LassoShelling lq = new LassoShelling(pentDm);
        lq.shell();
        QuartetSystem quartets = lq.getQuartets(true);
        //Test that matrix is complete
        assertEquals((pentMatrix.length * (pentMatrix.length -1)) * 0.5,
                lq.getMatrix().getMap().entrySet().stream().filter(e -> e.getValue() > 0).count(), 0.01);
        //Test that correct number of quartets
        long expectedCombos = CombinatoricsUtils.factorial(pentMatrix.length) / (CombinatoricsUtils.factorial(4) * CombinatoricsUtils.factorial(pentMatrix.length - 4));
        assertEquals(expectedCombos, quartets.getQuartets().size());
    }

    @Test
    public void retrieveAdditive() {
        //Define a complete additive metric
        double[][] additiveMatrix = {{0, 2, 4, 4, 3}, {2, 0, 4, 4, 3}, {4, 4, 0, 2, 3}, {4, 4, 2, 0, 3}, {3, 3, 3, 3, 0}};
        //Convert this into a support graph
        LassoDistanceGraph graph = new LassoDistanceGraph(new FlexibleDistanceMatrix(additiveMatrix));
        //Get a chordal subgraph of this
        //Enrich
        DistanceMatrix complete = new LassoShelling(graph).altShell();
        double[][] derived = complete.getMatrix(Comparator.naturalOrder());
        //Check the input and output metrics are equivalent
        for(int i = 0; i < additiveMatrix.length; i++) {
            assertArrayEquals(additiveMatrix[i], derived[i], 0.001);
        }
        //Output to see if splitstree constructs correct tree
    }

    @Test
    public void largeShelling() {
        //Define a complete additive metric
        double[][] diamonds = {
                {0, 2, 4, 4, 4, 0, 0},
                {2, 0, 4, 0, 0, 0, 0},
                {4, 4, 0, 2, 0, 0, 0},
                {4, 0, 2, 0, 4, 5, 0},
                {4, 0, 0, 4, 0, 3, 3},
                {0, 0, 0, 5, 3, 0, 2},
                {0, 0, 0, 0, 3, 2, 0}
        };
        double[][] additive = {
                {0, 2, 4, 4, 4, 5, 5},
                {2, 0, 4, 4, 4, 5, 5},
                {4, 4, 0, 2, 4, 5, 5},
                {4, 4, 2, 0, 4, 5, 5},
                {4, 4, 4, 4, 0, 3, 3},
                {5, 5, 5, 5, 3, 0, 2},
                {5, 5, 5, 5, 3, 2, 0}
        };
        //Convert this into a support graph
        LassoDistanceGraph graph = new LassoDistanceGraph(new FlexibleDistanceMatrix(diamonds));
        //Enrich
        DistanceMatrix complete = new LassoShelling(graph).altShell();
        double[][] derived = complete.getMatrix(Comparator.naturalOrder());
        //Check the input and output metrics are equivalent
        for(int i = 0; i < additive.length; i++) {
            assertArrayEquals(additive[i], derived[i], 0.001);
        }
    }
}