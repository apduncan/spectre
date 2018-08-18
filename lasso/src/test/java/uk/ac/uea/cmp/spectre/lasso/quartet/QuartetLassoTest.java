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

package uk.ac.uea.cmp.spectre.lasso.quartet;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import uk.ac.uea.cmp.spectre.lasso.LassoShellingTest;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class QuartetLassoTest {
    @Before
    public void setup() {
        BasicConfigurator.configure();
        LogManager.getRootLogger().setLevel(Level.FATAL);
    }

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void run1() throws IOException {
        //Make some options
        QuartetLassoOptions options = new QuartetLassoOptions();
        options.setInput(FileUtils.toFile(LassoShellingTest.class.getResource("/ex-additive-diamonds.nex")));
        //Input tree is
        // 1 _    _ 3
        //    \__/
        // 2_/   \
        //      / \
        //     5  4
        File output = new File(folder.getRoot(), "output.nex");
        options.setOutput(output);
        QuartetLasso lasso = new QuartetLasso(options);
        //run
        lasso.run();
        //Check the output file has been written
        assertTrue(output.exists());
        //Check it is not empty
        final List<String> outputLines = FileUtils.readLines(output, "UTF-8").stream().map(String::trim).collect(Collectors.toList());
        //Check file is not empty
        assertTrue(!outputLines.isEmpty());
        //Trim whitespace from lines
        //Check all the expected quartets exist
        String[] expectedQuartets = {"1 2 : 3 4", "2 3 : 4 5", "1 2 : 4 5", "1 2 : 3 5", "1 2 : 4 5"};
        long countFound = Stream.of(expectedQuartets).filter(quart -> {
            return outputLines.stream().filter(line -> line.contains(quart)).count() > 0;
        }).count();
        assertEquals(expectedQuartets.length, countFound);
    }
}