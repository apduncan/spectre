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
import uk.ac.uea.cmp.spectre.core.io.nexus.NexusReader;
import uk.ac.uea.cmp.spectre.lasso.LassoDistanceGraph;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class TripletCoverBuilderTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void find() throws IOException {
          File input = FileUtils.toFile(UnrootedLassoTest.class.getResource("/macaque-additive-integer.nex"));
          DistanceMatrix dm = new NexusReader().readDistanceMatrix(input);
          //Compare matrix to expected
          LassoDistanceGraph cover = new TripletCoverBuilder(new LassoDistanceGraph(dm)).find();
          for (Map.Entry<Pair<Identifier, Identifier>, Double> entry : cover.getMap().entrySet()) {
              assertNotEquals(0, entry.getValue(),0.00000000000000000000000000000001);
          }
          for(Pair<Identifier, Identifier> edge: cover.getMap().keySet()) {
              Set<Identifier> neighbours = cover.getNeighbours(edge.getLeft());
              neighbours.retainAll(cover.getNeighbours(edge.getRight()));
              System.out.println(neighbours.size());
                  assertTrue(neighbours.size() < 3);
          }
          assertEquals((cover.getTaxa().size() * 2) - 3, cover.getMap().size());
    }
}