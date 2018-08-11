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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import uk.ac.uea.cmp.spectre.core.ds.Identifier;
import uk.ac.uea.cmp.spectre.core.ds.distance.DistanceMatrix;
import uk.ac.uea.cmp.spectre.core.ds.distance.FlexibleDistanceMatrix;
import uk.ac.uea.cmp.spectre.core.ds.distance.RandomDistanceGenerator;
import uk.ac.uea.cmp.spectre.core.io.nexus.Nexus;
import uk.ac.uea.cmp.spectre.core.io.nexus.NexusReader;
import uk.ac.uea.cmp.spectre.core.io.nexus.NexusWriter;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class UnrootedLassoTest {
    @Before
    public void setup() {
        Logger.getLogger(UnrootedLasso.class.getName()).setLevel(Level.FATAL);
    }

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void run() throws IOException {
        //Create a random matrix
        DistanceMatrix matrix = new RandomDistanceGenerator().generateDistances(30);
        //Delete random 20%
        matrix = deleteRandom(matrix, 20);
        //Save out
        File input = folder.newFile("random.nex");
        File output = folder.newFile("output.nex");
        NexusWriter writer = new NexusWriter();
        writer.writeDistanceMatrix(input, matrix);
        //Run unrooted lasso on this random file
        UnrootedLassoOptions options = new UnrootedLassoOptions(input, output, ChordalSubgraphFinder.SEED_TREE.BREADTH);
        UnrootedLasso lasso = new UnrootedLasso(options);
        lasso.run();
        //Check output file exists
        assertTrue(output.exists());
        //Check it is not empty
        List<String> outputLines = FileUtils.readLines(output, "UTF-8");
        assertTrue(outputLines.size() > 0);
        //Check it is complete
        FlexibleDistanceMatrix outputMatrix = new FlexibleDistanceMatrix(new NexusReader().readDistanceMatrix(output));
        assertEquals(outputMatrix.getMap().values().stream().filter(val -> val != 0).count(), (30*29)/2, 0.001);
    }

    @Test
    public void run_additive() throws IOException {
        File input = FileUtils.toFile(UnrootedLassoTest.class.getResource("/ex-additive-diamonds.nex"));
        File output = folder.newFile("output_additive.nex");
        UnrootedLassoOptions options = new UnrootedLassoOptions(input, output);
        UnrootedLasso lasso = new UnrootedLasso(options);
        lasso.run();
        //Check output file exists
        assertTrue(output.exists());
        //Check not empty
        List<String> outLines = FileUtils.readLines(output, "UTF-8");
        assertTrue(outLines.size() > 0);
        //Check this does contain the full tree metric
        //Load matrix
        Nexus fileContents = new NexusReader().parse(output);
        //Compare matrix to expected
        final double[][] expected = {{0, 3, 8, 15, 16}, {3, 0, 9, 16, 17}, {8, 9, 0, 15, 16}, {15, 16, 15, 0, 13},
                {16, 17, 16, 13, 0}};
        final double[][] derived = fileContents.getDistanceMatrix().getMatrix(Comparator.comparing(Identifier::getName));
        for(int i = 0; i < expected.length; i++) {
            assertArrayEquals(expected[i], derived[i], 0.01);
        }
    }

    @Test
    public void run_large_additive() throws IOException {
        File input = FileUtils.toFile(UnrootedLassoTest.class.getResource("/macaque-additive-integer.nex"));
        File output = folder.newFile("output_additive.nex");
        UnrootedLassoOptions options = new UnrootedLassoOptions(input, output);
        UnrootedLasso lasso = new UnrootedLasso(options);
        lasso.run();
        //Check output file exists
        assertTrue(output.exists());
        //Check not empty
        List<String> outLines = FileUtils.readLines(output, "UTF-8");
        assertTrue(outLines.size() > 0);
        //Check this does contain the full tree metric
        //Load matrix
        Nexus fileContents = new NexusReader().parse(output);
        //Compare matrix to expected
        Nexus original = new NexusReader().parse(input);
        DistanceMatrix derived = fileContents.getDistanceMatrix();
        List<Pair<Identifier, Identifier>> zeros = derived.getMap().entrySet().stream().filter(val -> val.getValue() == 0).map(val -> val.getKey()).collect(Collectors.toList());
//        assertEquals(0, zeros.size());
//        assertEquals(derived.getMap().values().stream().filter(val -> val != 0).count(), (derived.getTaxa().size() * (derived.getTaxa().size() -1))/2, 0.001);
        for(Map.Entry<Pair<Identifier, Identifier>, Double> entry: derived.getMap().entrySet()) {
            double dist = original.getDistanceMatrix().getDistance(entry.getKey().getRight().getName(), entry.getKey().getLeft().getName());
            if(original.getDistanceMatrix().getDistance(entry.getKey().getRight().getName(), entry.getKey().getLeft().getName()) != entry.getValue())
                System.out.println(entry + " | " + dist);
//            assertEquals(original.getDistanceMatrix().getDistance(entry.getKey().getRight().getName(), entry.getKey().getLeft().getName()), entry.getValue(), 0.001);
        }
    }

    private DistanceMatrix deleteRandom(DistanceMatrix matrix, int percent) {
        double noRemove = Double.valueOf(matrix.getMap().size()) * (Double.valueOf(percent) / 100);
        List<Pair<Identifier, Identifier>> edges = new ArrayList<Pair<Identifier, Identifier>>(matrix.getMap().keySet());
        Collections.shuffle(edges);
        for(int i = 0; i < Math.round(noRemove); i++) {
            matrix.setDistance(edges.get(i).getRight(), edges.get(i).getLeft(), 0);
        }
        return matrix;
    }
}