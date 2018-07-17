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
import org.junit.Test;
import uk.ac.uea.cmp.spectre.core.ds.Identifier;
import uk.ac.uea.cmp.spectre.core.ds.distance.DistanceMatrix;
import uk.ac.uea.cmp.spectre.core.ds.distance.FlexibleDistanceMatrix;
import uk.ac.uea.cmp.spectre.core.io.nexus.Nexus;
import uk.ac.uea.cmp.spectre.core.io.nexus.NexusReader;
import uk.ac.uea.cmp.spectre.core.io.nexus.NexusWriter;
import uk.ac.uea.cmp.spectre.lasso.LassoDistanceGraph;
import uk.ac.uea.cmp.spectre.lasso.LassoQuartets;
import uk.ac.uea.cmp.spectre.lasso.lasso.LassoTest;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ChordalSubgraphFinderTest {
    @Test
    public void spanningTree() {
        double[][] matrix = {{0, 1, 1, 0}, {1, 0, 1, 0}, {1, 1, 0, 0}, {1, 0, 0, 0}};
        LassoDistanceGraph lg = new LassoDistanceGraph(new FlexibleDistanceMatrix(matrix));
        //These two should contain all vertices in the graph, as all in one component
        List<Identifier> treeVertices = lg.depthFirstSearch(lg.getTaxa().get(0));
        assertTrue(treeVertices.containsAll(lg.getTaxa()));
        treeVertices = lg.breadthFirstSearch(lg.getTaxa().get(0));
        assertTrue(treeVertices.containsAll(lg.getTaxa()));

        //Repeat the same with one vertex which is not connected
        double[][] matrix2 = {{0, 1, 0}, {1, 0, 0}, {0, 0, 0}};
        LassoDistanceGraph lg2 = new LassoDistanceGraph(new FlexibleDistanceMatrix(matrix2));
        treeVertices = lg2.depthFirstSearch(lg2.getTaxa().get(0));
        //Should only contain 2 vertices
        assertEquals(2, treeVertices.size());
        assertTrue(treeVertices.contains(lg2.getTaxa().get(0)));
        assertTrue(treeVertices.contains(lg2.getTaxa().get(1)));
        //Should be only one vertex
        treeVertices = lg2.depthFirstSearch(lg2.getTaxa().get(2));
        assertEquals(1, treeVertices.size());
        assertTrue(treeVertices.contains(lg2.getTaxa().get(2)));
    }

    @Test
    public void find() {
        double[][] matrix = {{0, 2, 3, 0, 0}, {2, 0, 3, 4, 0}, {3, 3, 0, 3, 3}, {0, 4, 3, 0, 2}, {0, 0, 3, 2, 0}};
        LassoDistanceGraph lg = new LassoDistanceGraph(new FlexibleDistanceMatrix(matrix));
        ChordalSubgraphFinder cf = new ChordalSubgraphFinder(lg);
        LassoDistanceGraph chordalSub = cf.find();
        //Should have found all taxa
        assertTrue(chordalSub.getTaxa().containsAll(lg.getTaxa()));

        //This is a cycle of 4 taxa with no chord available. Should only contain 3 edges, any more would create cycle
        double[][] matrixCycle = {{0, 1, 1, 0}, {1, 0, 0, 1}, {1, 0, 0, 1}, {0, 1, 1, 0}};
        LassoDistanceGraph chordal2 = new ChordalSubgraphFinder(new LassoDistanceGraph(new FlexibleDistanceMatrix(matrixCycle))).find();
        assertEquals(3, chordal2.getMap().values().stream().filter(v -> v > 0).count());

        //This defines a diamond where the missing chord corresponds to taxa which form a cherry
        //This diamond should not appear in our chordal subgraphs, only want diamonds where missing distance is not
        //cherry in the quartet
        //Quartet is a b : c d, where each edge weight is 1
        double[][] cherry = {{0, 0, 3, 3}, {0, 0, 3, 3}, {3, 3, 0, 2}, {3, 3, 2, 0}};
        LassoDistanceGraph noDiamond = new ChordalSubgraphFinder(new LassoDistanceGraph(new FlexibleDistanceMatrix(cherry))).find();
        //Check the distance between a b is 0 (does not exist)
        assertEquals(0, noDiamond.getDistance("A", "B"), 0.01);
        //Check the diamond has not been formed (less than 5 edges)
        assertTrue(noDiamond.getMap().values().stream().filter(v -> v > 0).count() < 5);
    }
}
