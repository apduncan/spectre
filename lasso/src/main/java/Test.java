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

import uk.ac.uea.cmp.spectre.core.ds.distance.DistanceMatrix;
import uk.ac.uea.cmp.spectre.core.ds.distance.FlexibleDistanceMatrix;
import uk.ac.uea.cmp.spectre.core.ds.distance.RandomDistanceGenerator;
import uk.ac.uea.cmp.spectre.core.io.nexus.Nexus;
import uk.ac.uea.cmp.spectre.core.io.nexus.NexusWriter;

import java.io.File;
import java.io.IOException;

public class Test {
    public static void main(String[] args) {
        System.out.println("Long testing string here");
        String assign = "Separate string";
        DistanceMatrix fd = new RandomDistanceGenerator().generateDistances(100);
        Nexus nexus = new Nexus();
        nexus.setTaxa(fd.getTaxa());
        nexus.setDistanceMatrix(fd);
        try {
            new NexusWriter().writeDistanceMatrix(new File("/home/hal/Dropbox/Dissertation/random.nex"), fd);
        } catch (IOException err) {
            System.out.println("failed");
        }
    }
}