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

package uk.ac.uea.cmp.spectre.lasso;import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class LassoTest {
    @Before
    public void setup() {
        BasicConfigurator.configure();
       LogManager.getRootLogger().setLevel(Level.INFO);
    }

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void run1() throws IOException {
        //uk.ac.uea.cmp.spectre.lasso.Test lasso with adapted worked example from paper, so only a single output tree is possible
        File input = FileUtils.toFile(LassoTest.class.getResource("/example-mod.nex"));
        File output = new File(folder.getRoot(), "output.nex");
        LassoOptions options = new LassoOptions();
        options.setInput(input);
        options.setOutput(output);
        Lasso lasso = new Lasso(options);
        lasso.run();
        //Check that the output file exists
        assertTrue(output.exists());
        List<String> outputLines = FileUtils.readLines(output, "UTF-8");
        //Check file is not empty
        assertTrue(!outputLines.isEmpty());
        //Check strong lasso is as expected
        String[] expectedLasso = {  "[B -> E, 6.0]",  "[B -> D, 2.0]", "[C -> D, 2.0]", "[A -> D, 4.0]", "[B -> C, 2.0]" };
        String[] expectedNewicks = {
                "TREE tree1 = (((B:1.0,C:1.0,D:1.0):1.0,A:2.0):1.0,E:3.0):0.0;",
                "TREE tree1 = (((B:1.0,D:1.0,C:1.0):1.0,A:2.0):1.0,E:3.0):0.0;",
                "TREE tree1 = (((D:1.0,B:1.0,C:1.0):1.0,A:2.0):1.0,E:3.0):0.0;",
                "TREE tree1 = (((D:1.0,C:1.0,B:1.0):1.0,A:2.0):1.0,E:3.0):0.0;",
                "TREE tree1 = (((C:1.0,B:1.0,D:1.0):1.0,A:2.0):1.0,E:3.0):0.0;",
                "TREE tree1 = (((C:1.0,D:1.0,B:1.0):1.0,A:2.0):1.0,E:3.0):0.0;"
        };
        //Trim whitespace from outputlines
        outputLines = outputLines.stream().map(String::trim).collect(Collectors.toList());
        //Check strong lasso matches what is expected
        long matched = Stream.of(expectedLasso).filter(outputLines::contains).count();
        assertEquals(expectedLasso.length, matched);
        //Check tree is isomorphically equivalent to what is expected
        Optional<String> matchedTree = Stream.of(expectedNewicks).filter(outputLines::contains).findFirst();
        assertTrue(matchedTree.isPresent());
    }

    @Test
    public void run2() throws IOException {
        //uk.ac.uea.cmp.spectre.lasso.Test lasso when two disconnected components exist in the input
        File input = FileUtils.toFile(LassoTest.class.getResource("/disconnected.nex"));
        File output = new File(folder.getRoot(), "output.nex");
        LassoOptions options = new LassoOptions();
        options.setInput(input);
        options.setOutput(output);
        Lasso lasso = new Lasso(options);
        lasso.run();
        //Check that the output file exists
        assertTrue(output.exists());
        List<String> outputLines = FileUtils.readLines(output, "UTF-8");
        outputLines.forEach(System.out::println);
        //Check file is not empty
        assertTrue(!outputLines.isEmpty());
        //Check strong lasso is as expected
        String[] expectedLasso = {  "[C -> D, 1.0]",  "[D -> E, 1.0]", "[C -> E, 1.0]" };
        String[] expectedNewicks = {
                "TREE tree1 = (E:0.5,C:0.5,D:0.5):0.0;",
                "TREE tree1 = (E:0.5,D:0.5,C:0.5):0.0;",
                "TREE tree1 = (D:0.5,E:0.5,C:0.5):0.0;",
                "TREE tree1 = (D:0.5,C:0.5,E:0.5):0.0;",
                "TREE tree1 = (C:0.5,E:0.5,D:0.5):0.0;",
                "TREE tree1 = (C:0.5,D:0.5,E:0.5):0.0;"
        };
        //Trim whitespace from outputlines
        outputLines = outputLines.stream().map(String::trim).collect(Collectors.toList());
        //Check strong lasso matches what is expected
        long matched = Stream.of(expectedLasso).filter(outputLines::contains).count();
        assertEquals(expectedLasso.length, matched);
        //Check tree is isomorphically equivalent to what is expected
        Optional<String> matchedTree = Stream.of(expectedNewicks).filter(outputLines::contains).findFirst();
        assertTrue(matchedTree.isPresent());
    }
}