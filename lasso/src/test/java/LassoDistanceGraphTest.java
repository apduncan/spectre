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

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.ac.uea.cmp.spectre.core.ds.Identifier;
import uk.ac.uea.cmp.spectre.core.ds.distance.FlexibleDistanceMatrix;
import uk.ac.uea.cmp.spectre.core.ds.distance.RandomDistanceGenerator;
import uk.ac.uea.cmp.spectre.core.ds.tree.newick.NewickNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class LassoDistanceGraphTest {
    private LassoDistanceGraph graph;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        this.graph = new LassoDistanceGraph(new RandomDistanceGenerator().generateDistances(100));
    }

    @Test
    public void getNeighbours() {
        //Graph at start should be complete, so neighbours should be |vertices|-1
        int count = this.graph.getNeighbours(this.graph.getTaxa().get(0)).size();
        assertEquals(count, this.graph.getTaxa().size() -1);

        //Remove an edge, test that Identifier does not appear in results
        this.graph.removeDistance(this.graph.getTaxa().get(0), this.graph.getTaxa().get(1));
        Set<Identifier> neighbours = this.graph.getNeighbours(this.graph.getTaxa().get(0));
        assertFalse(neighbours.contains(this.graph.getTaxa().get(1)));
        //test that expected number of neighbours returned
        count = neighbours.size();
        assertEquals(count, this.graph.getTaxa().size() - 2);
        //Vertex with no neighbours
        neighbours.forEach(vertex -> this.graph.removeDistance(this.graph.getTaxa().get(0), vertex));
        assertEquals(0, this.graph.getNeighbours(this.graph.getTaxa().get(0)).size());
    }

    @Test
    public void removeDistance() {
        //delete a connection from taxa 1 to 2
        this.graph.removeDistance(this.graph.getTaxa().get(0), this.graph.getTaxa().get(1));
        double missing = this.graph.getDistance(this.graph.getTaxa().get(0), this.graph.getTaxa().get(1));
        double present = this.graph.getDistance(this.graph.getTaxa().get(0), this.graph.getTaxa().get(2));

        //Deleted edge missing?
        assertEquals(0, missing, 0.001);

        //Edge present?
        assertNotEquals(0, present, 0.001);
    }

    @Test
    public void retainMinEdges() {
        double[][] matrix = new double[][] { {0, 2, 3, 0}, {2, 0, 2, 4}, {3, 2, 0, 0}, {0, 4, 0, 0} };
        double[][] reducedMatrix = new double[][] { {0, 2, 0, 0}, {2, 0, 2, 0}, {0, 2, 0, 0}, {0, 0, 0, 0}};
        LassoDistanceGraph lg = new LassoDistanceGraph(new FlexibleDistanceMatrix(matrix));
        lg.retainMinEdges();
        double[][] postRetain = lg.getMatrix();
        //Now neighbours should be 1: 2, 2: 1, 3, 3: 2, 4: empty
        for(int i = 0; i < matrix.length; i++) {
            assertArrayEquals(reducedMatrix[i], postRetain[i], 0.001);
        }
    }

    @Test
    public void isTaxon() {
        Identifier wrong = new Identifier(-2);
        Identifier right = this.graph.getTaxa().get(0);
        Identifier copy = new Identifier(right);
        //These are taxa
        assertTrue(this.graph.isTaxon(right));
        assertTrue(this.graph.isTaxon(copy));
        //Should throw exception as it does not exist in graph
        exception.expect(IllegalStateException.class);
        this.graph.isTaxon(wrong);
        //TODO: TEST FOR IDENTIFIER WHICH MAPS TO CLUSTER
    }

    @Test
    public void joinCluster() {
        double[][] matrix = new double[][] { {0, 2, 2, 0}, {2, 0, 2, 0}, {2, 2, 0, 4}, {0, 0, 4, 0} };
        LassoDistanceGraph lg = new LassoDistanceGraph(new FlexibleDistanceMatrix(matrix));
        List<Identifier> cluster = new ArrayList<>();
        cluster.add(lg.getTaxa().get(0));
        cluster.add(lg.getTaxa().get(1));
        cluster.add(lg.getTaxa().get(2));
        LassoTree clustered = lg.joinCluster(cluster, DistanceUpdaterFactory.MODAL);
        //Early test to see if functioning at all
        for(NewickNode child : clustered.getBranches()) {
            assertEquals(1, child.getLength(), 0.001);
        }
        cluster = new ArrayList<>();
        cluster.add(lg.getTaxa().get(0));
        cluster.add(lg.getTaxa().get(1));
        clustered = lg.joinCluster(cluster, DistanceUpdaterFactory.MODAL);
        clustered.removeInternalIdentifier();
        System.out.println(clustered);
    }
}