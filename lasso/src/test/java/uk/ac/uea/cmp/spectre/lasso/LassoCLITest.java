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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class LassoCLITest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    //@Test
    public void testTree1() throws IOException {

        File outputDir = temporaryFolder.getRoot();
        File outputFile = new File(outputDir.getAbsolutePath() + "output.nex");

        File testFile1 = FileUtils.toFile(LassoCLITest.class.getResource("/random100.nex"));

        LassoCLI.main(new String[]{
                "-o", outputFile.getAbsolutePath(),
                testFile1.getAbsolutePath()
        });

        assertTrue(outputFile.exists());
        assertTrue(FileUtils.readLines(outputFile, "UTF-8").size() > 0);
    }

    @Test
    public void testTree2() throws IOException {
        //uk.ac.uea.cmp.spectre.lasso.Test with more complex options
        File outputDir = temporaryFolder.getRoot();
        File outputFile = new File(outputDir.getAbsolutePath() + "output.nex");

        File testFile1 = FileUtils.toFile(LassoCLITest.class.getResource("/example-mod.nex"));

        LassoCLI.main(new String[]{
                "-o", outputFile.getAbsolutePath(),
                "-r", "1",
                "-du", "MODAL",
                "-cf", "HEURISTIC",
                testFile1.getAbsolutePath()
        });

        assertTrue(outputFile.exists());
        assertTrue(FileUtils.readLines(outputFile, "UTF-8").size() > 0);
    }

    //@Test
    public void testTree5() throws IOException {
        //uk.ac.uea.cmp.spectre.lasso.Test with more complex options
        File outputDir = temporaryFolder.getRoot();
        File outputFile = new File(outputDir.getAbsolutePath() + "output.nex");

        File testFile1 = FileUtils.toFile(LassoCLITest.class.getResource("/paradoxus-part-question.nex"));

        LassoCLI.main(new String[]{
                "-o", outputFile.getAbsolutePath(),
                testFile1.getAbsolutePath()
        });

        assertTrue(outputFile.exists());
        assertTrue(FileUtils.readLines(outputFile, "UTF-8").size() > 0);
    }

    //@uk.ac.uea.cmp.spectre.lasso.Test
    public void testTree3() throws IOException {
        //Run with invalid args
        File outputDir = temporaryFolder.getRoot();
        File outputFile = new File(outputDir.getAbsolutePath() + "output.nex");

        File testFile1 = FileUtils.toFile(LassoCLITest.class.getResource("/random100.nex"));

        LassoCLI.main(new String[]{
                "-o", outputFile.getAbsolutePath(),
                "-r", "5",
                "-a", "15",
                "-du", "MODAL",
                "-cf", "FALSE",
                testFile1.getAbsolutePath()
        });

        assertTrue(outputFile.exists());
        assertTrue(FileUtils.readLines(outputFile, "UTF-8").size() > 0);
    }

    //@uk.ac.uea.cmp.spectre.lasso.Test
    public void testTree4() throws IOException {
        //Run with missing input file
        File outputDir = temporaryFolder.getRoot();
        File outputFile = new File(outputDir.getAbsolutePath() + "output.nex");

        File testFile1 = FileUtils.toFile(LassoCLITest.class.getResource("/nonexistent.nex"));

        LassoCLI.main(new String[]{
                "-o", outputFile.getAbsolutePath(),
                "/home/hal/missing.nex"
        });

        assertTrue(!outputFile.exists());
    }
}