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

import com.sun.corba.se.spi.ior.IdentifiableContainerBase;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import uk.ac.uea.cmp.spectre.core.ds.Identifier;
import uk.ac.uea.cmp.spectre.core.ds.distance.DistanceMatrix;
import uk.ac.uea.cmp.spectre.core.ds.distance.RandomDistanceGenerator;
import uk.ac.uea.cmp.spectre.core.io.nexus.NexusWriter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class UnrootedLassoTest {
    @Before
    public void setup() {
        BasicConfigurator.configure();
        LogManager.getRootLogger().setLevel(Level.INFO);
    }

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void run() throws IOException {
        //Create a random matrix
        DistanceMatrix matrix = new RandomDistanceGenerator().generateDistances(90);
        //Delete random 20%
        matrix = deleteRandom(matrix, 90);
        //Save out
        File input = folder.newFile("random.nex");
        File output = folder.newFile("output.nex");
        NexusWriter writer = new NexusWriter();
        writer.writeDistanceMatrix(input, matrix);
        //Run unrooted lasso on this random file
        UnrootedLassoOptions options = new UnrootedLassoOptions(input, output, ChordalSubgraphFinder.SEED_TREE.DEPTH);
        UnrootedLasso lasso = new UnrootedLasso(options);
        lasso.run();
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