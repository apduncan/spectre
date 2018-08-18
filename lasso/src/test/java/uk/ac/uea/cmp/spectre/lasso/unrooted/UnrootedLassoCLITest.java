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
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import uk.ac.uea.cmp.spectre.core.ds.Identifier;
import uk.ac.uea.cmp.spectre.core.io.nexus.Nexus;
import uk.ac.uea.cmp.spectre.core.io.nexus.NexusReader;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;

import static org.junit.Assert.*;

public class UnrootedLassoCLITest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void before() {
        Logger.getLogger(UnrootedLasso.class.getName()).setLevel(Level.FATAL);
        Logger.getLogger(UnrootedLassoCLI.class.getName()).setLevel(Level.FATAL);
    }

    @Test
    public void runCLI() throws IOException {
        //uk.ac.uea.cmp.spectre.rooted.Test with more complex options
        File outputDir = temporaryFolder.getRoot();
        File outputFile = new File(outputDir.getAbsolutePath() + "output.nex");

        File testFile1 = FileUtils.toFile(UnrootedLassoCLITest.class.getResource("/ex-additive-diamonds.nex"));

        UnrootedLassoCLI.main(new String[]{
                "-o", outputFile.getAbsolutePath(),
                testFile1.getAbsolutePath()
        });

        assertTrue(outputFile.exists());
        assertTrue(FileUtils.readLines(outputFile, "UTF-8").size() > 0);
        Nexus fileContents = new NexusReader().parse(outputFile);
        //Compare matrix to expected
        final double[][] expected = {{0, 3, 8, 15, 16}, {3, 0, 9, 16, 17}, {8, 9, 0, 15, 16}, {15, 16, 15, 0, 13},
                {16, 17, 16, 13, 0}};
        final double[][] derived = fileContents.getDistanceMatrix().getMatrix(Comparator.comparing(Identifier::getName));
        for(int i = 0; i < expected.length; i++) {
            assertArrayEquals(expected[i], derived[i], 0.01);
        }
    }
}