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
import uk.ac.uea.cmp.spectre.lasso.LassoDistanceGraph;
import uk.ac.uea.cmp.spectre.lasso.LassoShelling;
import uk.ac.uea.cmp.spectre.lasso.quartet.QuartetLassoResult;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

public class IncrementalLassoTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void find() {
    }

    private void random_integers(File output, int max) throws IOException {
        FlexibleDistanceMatrix dm = new FlexibleDistanceMatrix(new RandomDistanceGenerator().generateDistances(50));
        List<Pair<Identifier, Identifier>> keys = dm.getMap().keySet().stream().collect(Collectors.toList());
        for (Pair<Identifier, Identifier> dist : keys) {
            dm.setDistance(dist.getRight(), dist.getLeft(), ThreadLocalRandom.current().nextInt(1, max));
        }
        new NexusWriter().writeDistanceMatrix(output, dm);
    }

    private DistanceMatrix deleteRandom(DistanceMatrix matrix, int percent) {
        double noRemove = Double.valueOf(matrix.getMap().size()) * (Double.valueOf(percent) / 100);
        List<Pair<Identifier, Identifier>> edges = new ArrayList<Pair<Identifier, Identifier>>(matrix.getMap().keySet());
        Collections.shuffle(edges);
        for (int i = 0; i < Math.round(noRemove); i++) {
            matrix.setDistance(edges.get(i).getRight(), edges.get(i).getLeft(), 0);
        }
        return matrix;
    }

    @Test
    public void random_ints_out() throws IOException {
        File out = new File("/home/hal/Dropbox/Dissertation/test_data/random_ints.nex");
        random_integers(out, 20);
        DistanceMatrix dm = new NexusReader().readDistanceMatrix(out);
        dm = deleteRandom(dm, 20);
    }

    @Test
    public void run_macaque() throws IOException {
        File input = FileUtils.toFile(UnrootedLassoTest.class.getResource("/macaque-additive-integer.nex"));
        File output = folder.newFile("output_an_additive.nex");
        //Delete some of the values in additive matrix
        DistanceMatrix partial = deleteRandom(new NexusReader().readDistanceMatrix(input), 10);
        File inputPartial = folder.newFile("partial.nex");
        new NexusWriter().writeDistanceMatrix(inputPartial, partial);
        Nexus original = new NexusReader().parse(input);
        Pair<LassoDistanceGraph, Set<Pair<Identifier, Identifier>>> derivedPair = new IncrementalLasso(new LassoDistanceGraph(original.getDistanceMatrix())).find();
        DistanceMatrix derived = derivedPair.getLeft();
        new NexusWriter().writeDistanceMatrix(folder.newFile("macaque.nex"), derived);
        for (Map.Entry<Pair<Identifier, Identifier>, Double> entry : derived.getMap().entrySet()) {
            double dist = original.getDistanceMatrix().getDistance(entry.getKey().getRight().getName(), entry.getKey().getLeft().getName());
        }
        for(Pair<Identifier, Identifier> cord: derivedPair.getRight()) {
            double orig = original.getDistanceMatrix().getDistance(cord.getLeft().getName(), cord.getRight().getName());
            double der = derived.getDistance(cord.getLeft().getName(), cord.getRight().getName());
        }
    }

    @Test
    public void run_random() throws IOException {
        File input = FileUtils.toFile(UnrootedLassoTest.class.getResource("/macaque-additive-integer.nex"));
        File random = folder.newFile("random.nex");
        File output = folder.newFile("output_an_additive.nex");
        random_integers(random, 15);
        input = random;
        //Delete some of the values in additive matrix
        DistanceMatrix partial = deleteRandom(new NexusReader().readDistanceMatrix(input), 10);
        File inputPartial = folder.newFile("partial.nex");
        new NexusWriter().writeDistanceMatrix(inputPartial, partial);
        Nexus original = new NexusReader().parse(input);
        Pair<LassoDistanceGraph, Set<Pair<Identifier, Identifier>>> derivedPair = new IncrementalLasso(new LassoDistanceGraph(original.getDistanceMatrix())).find();
        DistanceMatrix derived = derivedPair.getLeft();
        new NexusWriter().writeDistanceMatrix(new File("/home/hal/omigod-large.nex"), derived);
        QuartetLassoResult qr = new QuartetLassoResult();
        qr.setQuartetBlock(new LassoShelling(derived).getQuartetsAsString(true));
        new NexusWriter().writeDistanceMatrix(new File("/home/hal/omigod-large-partial.nex"), partial);
        for (Map.Entry<Pair<Identifier, Identifier>, Double> entry : derived.getMap().entrySet()) {
            double dist = original.getDistanceMatrix().getDistance(entry.getKey().getRight().getName(), entry.getKey().getLeft().getName());
        }
        for(Pair<Identifier, Identifier> cord: derivedPair.getRight()) {
            double orig = original.getDistanceMatrix().getDistance(cord.getLeft().getName(), cord.getRight().getName());
            double der = derived.getDistance(cord.getLeft().getName(), cord.getRight().getName());
        }
    }
}