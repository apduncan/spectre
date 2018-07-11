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
import org.junit.Test;
import uk.ac.uea.cmp.spectre.core.ds.distance.DistanceMatrix;
import uk.ac.uea.cmp.spectre.core.ds.distance.FlexibleDistanceMatrix;
import uk.ac.uea.cmp.spectre.core.ds.distance.RandomDistanceGenerator;
import uk.ac.uea.cmp.spectre.core.io.nexus.Nexus;
import uk.ac.uea.cmp.spectre.core.io.nexus.NexusReader;
import uk.ac.uea.cmp.spectre.core.io.nexus.NexusWriter;
import uk.ac.uea.cmp.spectre.lasso.Lasso;
import uk.ac.uea.cmp.spectre.lasso.LassoDistanceGraph;
import uk.ac.uea.cmp.spectre.lasso.LassoTest;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ChordalSubgraphFinderTest {
    @Test
    public void spanningTree() {
        double[][] matrix = {{0, 1, 1, 0}, {1, 0, 1, 0}, {1, 1, 0, 0}, {1, 0, 0, 0}};
        LassoDistanceGraph lg = new LassoDistanceGraph(new FlexibleDistanceMatrix(matrix));
        ChordalSubgraphFinder chordal = new ChordalSubgraphFinder();
        chordal.find(lg);
    }

    @Test
    public void find() {
        double[][] matrix = {{0, 2, 3, 0, 0}, {2, 0, 3, 4, 0}, {3, 3, 0, 3, 3}, {0, 4, 3, 0, 2}, {0, 0, 3, 2, 0}};
        LassoDistanceGraph lg = new LassoDistanceGraph(new FlexibleDistanceMatrix(matrix));
        ChordalSubgraphFinder cf = new ChordalSubgraphFinder();
//        LassoDistanceGraph chordalSub = cf.find(lg);
//        System.out.println(chordalSub);
        try {
            NexusReader reader = new NexusReader();
            File input = FileUtils.toFile(LassoTest.class.getResource("/paradoxus-part-question.nex"));
            Nexus file = reader.parse(input);
            lg = new LassoDistanceGraph(new FlexibleDistanceMatrix(file.getDistanceMatrix()));
        } catch (IOException io) {

        }
        LassoDistanceGraph chordalSub = cf.find(lg);
        List<DistanceMatrix> triplets = new TripletCoverFinder(chordalSub).findTripletCovers();
        //Attempt to complete
        LassoQuartets shell = new LassoQuartets(triplets.get(0));
        DistanceMatrix dm = shell.altEnrichMatrix();
        System.out.println(dm.getMap().size());
        File output = new File("/home/hal/complete-paradoxus.nex");
        NexusWriter writer = new NexusWriter();
        writer.appendHeader();
        writer.appendLine();
        writer.append(dm.getTaxa());
        writer.appendLine();
        writer.append(dm);
        try {
            writer.write(output);
        } catch (IOException e) {

        }
    }
}
