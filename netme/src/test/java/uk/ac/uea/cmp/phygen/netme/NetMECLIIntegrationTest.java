/*
 * Phylogenetics Tool suite
 * Copyright (C) 2013  UEA CMP Phylogenetics Group
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package uk.ac.uea.cmp.phygen.netme;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: Dan
 * Date: 28/04/13
 * Time: 19:15
 * To change this template use File | Settings | File Templates.
 */
public class NetMECLIIntegrationTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testTreeBees() throws IOException {

        File outputDir = temporaryFolder.getRoot();

        File distancesFile = FileUtils.toFile(NetMECLIIntegrationTest.class.getResource("/bees.nex"));
        File coFile = FileUtils.toFile(NetMECLIIntegrationTest.class.getResource("/bees-tree.nex"));

        NetMECLI.main(new String[]{
                "--distances_file", distancesFile.getAbsolutePath(),
                "--circular_ordering_file", coFile.getAbsolutePath(),
                "--output", outputDir.getAbsolutePath()
        });

        assertTrue(outputDir.listFiles().length == 2);
    }

    @Test
    public void testColors() throws IOException {

        File outputDir = temporaryFolder.getRoot();

        File distancesFile = FileUtils.toFile(NetMECLIIntegrationTest.class.getResource("/colors.nex"));
        File coFile = FileUtils.toFile(NetMECLIIntegrationTest.class.getResource("/colors-network.nex"));

        NetMECLI.main(new String[]{
                "--distances_file", distancesFile.getAbsolutePath(),
                "--circular_ordering_file", coFile.getAbsolutePath(),
                "--output", outputDir.getAbsolutePath()
        });

        assertTrue(outputDir.listFiles().length == 2);
    }

    @Test
    public void test6taxa() throws IOException {

        File outputDir = temporaryFolder.getRoot();

        File distancesFile = FileUtils.toFile(NetMECLIIntegrationTest.class.getResource("/test_MEGreedy6.nex"));
        File coFile = FileUtils.toFile(NetMECLIIntegrationTest.class.getResource("/test_MEGreedy6_Splits.nex"));

        NetMECLI.main(new String[]{
                "--distances_file", distancesFile.getAbsolutePath(),
                "--circular_ordering_file", coFile.getAbsolutePath(),
                "--output", outputDir.getAbsolutePath()
        });

        assertTrue(outputDir.listFiles().length == 2);
    }
}
