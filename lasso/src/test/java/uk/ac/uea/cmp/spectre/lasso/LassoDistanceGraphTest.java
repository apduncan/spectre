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

package uk.ac.uea.cmp.spectre.lasso;import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.ac.uea.cmp.spectre.core.ds.Identifier;
import uk.ac.uea.cmp.spectre.core.ds.distance.FlexibleDistanceMatrix;
import uk.ac.uea.cmp.spectre.core.ds.distance.RandomDistanceGenerator;
import uk.ac.uea.cmp.spectre.core.ds.tree.newick.NewickNode;
import uk.ac.uea.cmp.spectre.lasso.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

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
        //uk.ac.uea.cmp.spectre.lasso.Test that deleting non minimum edges in graph with all edges having equal weight works, does not delete all
        //edges
        lg.retainMinEdges();
        for(int i = 0; i < matrix.length; i++) {
            assertArrayEquals(reducedMatrix[i], lg.getMatrix()[i], 0.001);
        }
        //uk.ac.uea.cmp.spectre.lasso.Test that deleting non minimum edges in a graph with no edges does not cause an exception
        lg.removeDistance(lg.getTaxa().getByName("A"), lg.getTaxa().getByName("B"));
        lg.removeDistance(lg.getTaxa().getByName("B"), lg.getTaxa().getByName("C"));
        exception.expect(IllegalStateException.class);
        lg.retainMinEdges();
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
        //Join a cluster, and test the returned identifer
        Set<Identifier> cluster = new HashSet<>();
        cluster.add(this.graph.getTaxa().get(0));
        cluster.add(this.graph.getTaxa().get(1));
        //uk.ac.uea.cmp.spectre.lasso.Test a vertex which should be a cluster rather than a taxon
        LassoTree joined = this.graph.joinCluster(new ArrayList<>(cluster), new ModalDistanceUpdater(new LassoOptions()));
        assertFalse(this.graph.isTaxon(joined.getTaxon()));
    }

    @Test
    public void joinCluster() {
        double[][] matrix = new double[][]{{0, 2, 2, 0}, {2, 0, 2, 0}, {2, 2, 0, 4}, {0, 0, 4, 0}};
        double[][] expectedUpdate = new double[][]{{0, 4}, {4, 0}};
        LassoDistanceGraph lg = new LassoDistanceGraph(new FlexibleDistanceMatrix(matrix));
        Identifier a = lg.getTaxa().getByName("A");
        Identifier b = lg.getTaxa().getByName("B");
        Identifier c = lg.getTaxa().getByName("C");
        Identifier d = lg.getTaxa().getByName("D");
        Pair<Identifier, Identifier> ab = new ImmutablePair(a, b);
        Pair<Identifier, Identifier> ac = new ImmutablePair(a, c);
        Pair<Identifier, Identifier> bc = new ImmutablePair(b, c);
        Pair<Identifier, Identifier> cd = new ImmutablePair(c, d);
        List<Identifier> cluster = new ArrayList<>();
        cluster.add(lg.getTaxa().get(0));
        cluster.add(lg.getTaxa().get(1));
        cluster.add(lg.getTaxa().get(2));
        LassoTree clustered = lg.joinCluster(cluster, DistanceUpdaterFactory.MODAL.get(new LassoOptions()));
        //Early test to see if functioning at all
        for (NewickNode child : clustered.getBranches()) {
            assertEquals(1, child.getLength(), 0.001);
        }
        LassoTree noInternal = new LassoTree(clustered);
        System.out.println(clustered);
        noInternal.removeInternalIdentifier();
        //Check tree is expected tree
        String[] validNewicks = {"(C:1.0,B:1.0,A:1.0):0.0", "(C:1.0,A:1.0,B:1.0):0.0", "(A:1.0,B:1.0,C:1.0):0.0", "(A:1.0,C:1.0,B:1.0):0.0"
                , "(B:1.0,C:1.0,A:1.0):0.0", "(B:1.0,A:1.0,C:1.0):0.0"};
        Boolean validTopo = Stream.of(validNewicks).filter(s -> noInternal.toString().equals(s)).count() > 0;
        System.out.println(noInternal.toString());
        assertTrue(validTopo);
        //Check graph has expected matrix
        for (int i = 0; i < expectedUpdate.length; i++) {
            assertArrayEquals(expectedUpdate[i], lg.getMatrix()[i], 0.001);
        }
        //Check distances used has been updated to the expected set
        Set<Pair<Identifier, Identifier>> expected = new HashSet<>();
        expected.add(ab);
        expected.add(ac);
        expected.add(bc);
        assertTrue(lg.getDistancesUsed().containsAll(expected) && lg.getDistancesUsed().size() == expected.size());
        //Second round of clustering
        cluster = new ArrayList<>();
        cluster.add(lg.getTaxa().get(0));
        cluster.add(lg.getTaxa().get(1));
        clustered = lg.joinCluster(cluster, DistanceUpdaterFactory.MODAL.get(new LassoOptions()));
        LassoTree topoCompare = new LassoTree(clustered);
        topoCompare.removeInternalIdentifier();
        clustered.removeInternalIdentifier();
        validNewicks = Stream.of(validNewicks).map(s -> "(D:2.0," + s.substring(0, s.length() - 4) + ":1.0):0.0").toArray(String[]::new);
        validTopo = Stream.of(validNewicks).filter(s -> topoCompare.toString().equals(s)).count() > 0;
        //Check returned tree is in the expected format
        assertTrue(validTopo);
        //Check matrix matches expected
        assertEquals(1, lg.getTaxa().size(), 0.001);
        expected.add(cd);
        assertTrue(lg.getDistancesUsed().containsAll(expected) && lg.getDistancesUsed().size() == expected.size());
        System.out.println(clustered);
    }

    @Test
    public void getMinEdgeWeight() {
        double[][] matrix = new double[][] { {0, 2, 3, 0}, {2, 0, 2, 4}, {3, 2, 0, 0}, {0, 4, 0, 0} };
        double[][] oneWeight = new double[][]{{0, 4}, {4, 0}};
        LassoDistanceGraph lg = new LassoDistanceGraph(new FlexibleDistanceMatrix(matrix));
        assertEquals(lg.getMinEdgeWeight(), 2, 0.001);
        lg = new LassoDistanceGraph(new FlexibleDistanceMatrix(oneWeight));
        assertEquals(lg.getMinEdgeWeight(), 4, 0.001);
        lg.removeDistance(lg.getTaxa().get(0), lg.getTaxa().get(1));
        exception.expect(IllegalStateException.class);
        lg.getMinEdgeWeight();
    }

    @Test
    public void getCluster() {
        double[][] matrix = new double[][]{{0, 2, 2, 0}, {2, 0, 2, 0}, {2, 2, 0, 4}, {0, 0, 4, 0}};
        LassoDistanceGraph lg = new LassoDistanceGraph(new FlexibleDistanceMatrix(matrix));
        //Get a cluster which is just a single taxon
        LassoTree cluster = lg.getCluster(lg.getTaxa().get(0));
        assertEquals(cluster.toString(), "A:0.0");
        Set<Identifier> join = new HashSet<>();
        join.add(lg.getTaxa().get(0));
        join.add(lg.getTaxa().get(1));
        join.add(lg.getTaxa().get(2));
        lg.joinCluster(new ArrayList<>(join), DistanceUpdaterFactory.MODAL.get(new LassoOptions()));
        //Get cluster which should map the joined cluster
        Identifier clusterVertex = lg.getTaxa().get(1);
        cluster = lg.getCluster(clusterVertex);
        String[] validNewicks = {"(C:1.0,B:1.0,A:1.0):0.0", "(C:1.0,A:1.0,B:1.0):0.0", "(A:1.0,B:1.0,C:1.0):0.0", "(A:1.0,C:1.0,B:1.0):0.0"
                , "(B:1.0,C:1.0,A:1.0):0.0", "(B:1.0,A:1.0,C:1.0):0.0"};
        LassoTree copy = new LassoTree(cluster);
        copy.removeInternalIdentifier();
        Boolean validTopo = Stream.of(validNewicks).filter(s -> copy.toString().equals(s)).count() > 0;
        assertTrue(validTopo);
        Identifier missing = new Identifier("uk.ac.uea.cmp.spectre.lasso.Test", -2);
        exception.expect(IllegalStateException.class);
        lg.getCluster(missing);
    }

    @Test
    public void getConnectedComponents() {
        double[][] matrix = new double[][]{{0, 1, 0, 0, 0}, {1, 0, 0, 0, 0}, {0, 0, 0, 1, 1}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}};
        LassoDistanceGraph lg = new LassoDistanceGraph(new FlexibleDistanceMatrix(matrix));
        List<LassoDistanceGraph> components = lg.getConnectedComponents();
        assertEquals(components.size(), 2);
        assertEquals(1, components.get(0).getMap().size());
        assertEquals(2, components.get(1).getMap().size());
        assertEquals(components.get(1).getDistance("D", "E"), 0, 0.01);
//        LassoDistanceGraph big = new LassoDistanceGraph(new RandomDistanceGenerator().generateDistances(300));
//        components = big.getConnectedComponents();
    }
}