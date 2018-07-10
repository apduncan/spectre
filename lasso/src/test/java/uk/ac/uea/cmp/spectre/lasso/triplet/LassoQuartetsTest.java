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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import uk.ac.uea.cmp.spectre.core.ds.Identifier;
import uk.ac.uea.cmp.spectre.core.ds.distance.DistanceMatrix;
import uk.ac.uea.cmp.spectre.core.ds.distance.FlexibleDistanceMatrix;
import uk.ac.uea.cmp.spectre.core.ds.distance.RandomDistanceGenerator;
import uk.ac.uea.cmp.spectre.core.ds.quad.quartet.QuartetSystem;
import uk.ac.uea.cmp.spectre.core.io.nexus.Nexus;
import uk.ac.uea.cmp.spectre.core.io.nexus.NexusReader;
import uk.ac.uea.cmp.spectre.core.io.nexus.NexusWriter;
import uk.ac.uea.cmp.spectre.lasso.LassoDistanceGraph;
import uk.ac.uea.cmp.spectre.lasso.LassoTest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class LassoQuartetsTest {

    @Test
    public void enrichMatrix() {
        double[][] matrix = { {0, 3, 8, 0}, {3, 0, 9, 10}, {8, 9, 0, 9}, {0, 10, 9, 0} };
        DistanceMatrix dm = new FlexibleDistanceMatrix(matrix);
        LassoQuartets lq = new LassoQuartets(dm);
        dm = lq.altEnrichMatrix();
        //distance between a and d should be set to 9
        assertEquals(dm.getDistance("A", "D"), 9, 0.01);

        //Test if it can convert a system of 3 triangles on 5 taxa to a complete graph
        //Has 3 missing distance rather than 1
        double[][] pentMatrix = { {0, 3, 8, 0, 0}, {3, 0, 9, 16, 0}, {8, 9, 0, 15, 16}, {0, 16, 15, 0, 13}, {0, 0, 16, 13, 0} };
        DistanceMatrix pentDm = new FlexibleDistanceMatrix(pentMatrix);
        lq = new LassoQuartets(pentDm);
        pentDm = lq.altEnrichMatrix();
        assertEquals(pentDm.getDistance("A", "D"), 15,0.01);
        assertEquals(pentDm.getDistance("A", "E"), 16, 0.01);
        assertEquals(pentDm.getDistance("B", "E"), 17, 0.01);
    }

    @Test
    public void timeComparison() {
        DistanceMatrix dm = new RandomDistanceGenerator().generateDistances(30);
        List<Pair<Identifier, Identifier>> pairs = new ArrayList<>(dm.getMap().keySet());
        Collections.shuffle(pairs);
        //delete 20
        System.out.println("Deleting edges");
        for(int i = 0; i < 90; i++) {
            dm.setDistance(pairs.get(i).getLeft(), pairs.get(i).getRight(), 0);
        }
        LassoQuartets lq = new LassoQuartets(dm);
        StopWatch timer = new StopWatch();
//        System.out.println("Begin standard enriching");
//        timer.start();
//        lq.enrichMatrix();
//        System.out.println("Finished enriching standard");
//        timer.stop();
//        System.out.println("Time: " + timer.getTime());
//        timer.reset();
        lq = new LassoQuartets(dm);
        timer.start();
        lq.altEnrichMatrix();
        timer.stop();
        System.out.println("Finished enriching alternate");
        System.out.println("Time: " + timer.getTime());

        QuartetSystem quartets = lq.getQuartets();
        System.out.println(quartets);
        NexusWriter writer = new NexusWriter();
        writer.appendHeader();
        writer.appendLine();
        writer.append(quartets.getTaxa());
        writer.appendLine();
        writer.append(quartets, false);
        try {
            writer.write(new File("/home/hal/test-quartet.nex"));
        } catch (IOException e) {
            System.out.println("Write err: "  + e.toString());
        }
    }

    @Test
    public void yeastTest() {
        File input = FileUtils.toFile(LassoTest.class.getResource("/paradoxus-part-question.nex"));
        File output = new File("/home/hal/quartet-yeast.nex");
        try {
            Nexus nexus = new NexusReader().parse(input);
            LassoDistanceGraph lg = new LassoDistanceGraph(new FlexibleDistanceMatrix(nexus.getDistanceMatrix()));
            LassoDistanceGraph original = new LassoDistanceGraph(lg);
            LassoQuartets lq = new LassoQuartets(lg);
            FlexibleDistanceMatrix dm = new FlexibleDistanceMatrix(lq.altEnrichMatrix());
            QuartetSystem quartets = lq.getQuartets();
            //now original method
            LassoDistanceGraph lg2 = new LassoDistanceGraph(original);
            LassoQuartets lq2 = new LassoQuartets(lg2);
            FlexibleDistanceMatrix dm2 = new FlexibleDistanceMatrix(lq2.enrichMatrix());
            QuartetSystem quartets2 = lq2.getQuartets();
            //output edges which are not equal, and whether they existed in the original matrix
            Set<Pair<Identifier, Identifier>> edges = dm.getMap().entrySet().stream().filter(e -> e.getValue() > 0)
                    .map(e -> e.getKey()).collect(Collectors.toSet());
            edges.addAll(dm2.getMap().entrySet().stream().filter(e-> e.getValue() > 0).map(e -> e.getKey()).collect(Collectors.toSet()));
            edges.stream().forEach(e -> {
                boolean notPresent = false;
                if(!(dm2.getMap().containsKey(e))) {
                    System.out.println("Edge " + e.toString() + " weight " + dm.getMap().get(e) + " not in dm2");
                    notPresent = true;
                }
                if(!(dm.getMap().containsKey(e))) {
                    System.out.println("Edge " + e.toString() + " weight " + dm2.getMap().get(e) + " not in dm1");
                    notPresent = true;
                }
                if(!notPresent) {
                    //Check distance equal
                    if(dm.getDistance(e.getLeft(), e.getRight()) != dm2.getDistance(e.getLeft(), e.getRight())) {
                        System.out.println(e.toString() + " not equal length");
                    }
                }
            });
            NexusWriter writer = new NexusWriter();
            writer.appendHeader();
            writer.appendLine();
            writer.append(quartets2.getTaxa());
            writer.appendLine();
            writer.append(quartets2, false);
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
        LassoQuartets lq = new LassoQuartets(pentDm);
        lq.enrichMatrix();
        QuartetSystem quartets = lq.getQuartets();
        System.out.println(quartets);
    }

    @Test
    public void randomDeletions() {
        DistanceMatrix dm = new RandomDistanceGenerator().generateDistances(100);
        List<Pair<Identifier, Identifier>> pairs = new ArrayList<>(dm.getMap().keySet());
        Collections.shuffle(pairs);
        //delete 20
        for(int i = 0; i < 20; i++) {
            dm.setDistance(pairs.get(i).getLeft(), pairs.get(i).getRight(), 0);
        }
        LassoQuartets lq = new LassoQuartets(dm);
        lq.altEnrichMatrix();
        System.out.println("Finished enriching");
        QuartetSystem quartets = lq.getQuartets();
        System.out.println(quartets);
    }

}