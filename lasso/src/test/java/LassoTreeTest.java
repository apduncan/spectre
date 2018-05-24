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

import org.junit.Before;
import org.junit.Test;
import uk.ac.uea.cmp.spectre.core.ds.Identifier;

import static org.junit.Assert.*;

public class LassoTreeTest {
    private LassoTree testTree;
    private LassoTree leafA;
    private LassoTree leafB;
    private LassoTree leafC;
    private LassoTree leafD;
    private LassoTree vertexV;
    private LassoTree vertexU;

    @Before
    public void setUp() throws Exception {
        //build a tree with a known structure to test on
        testTree = new LassoTree();
        //first leaf
        Identifier taxonA = new Identifier("A");
        leafA = new LassoTree();
        leafA.setTaxon(taxonA);
        leafA.setLength(7);
        testTree.addBranch(leafA);
        //internal v
        vertexV = new LassoTree();
        vertexV.setLength(4);
        testTree.addBranch(vertexV);
        //leaf b
        Identifier taxonB = new Identifier("B");
        leafB = new LassoTree();
        leafB.setTaxon(taxonB);
        leafB.setLength(3);
        vertexV.addBranch(leafB);
        //internal u
        vertexU = new LassoTree();
        vertexU.setLength(2);
        vertexV.addBranch(vertexU);
        //leaf c
        leafC = new LassoTree();
        Identifier taxonC = new Identifier("C");
        leafC.setTaxon(taxonC);
        leafC.setLength(1);
        vertexU.addBranch(leafC);
        //leaf D
        leafD = new LassoTree();
        Identifier taxonD = new Identifier("D");
        leafD.setLength(1);
        leafD.setTaxon(taxonD);
        leafD.addBranch(leafD);
    }

    @Test
    public void getRootHeight() {
        //test height of each internal node, root, and leaf
        //root
        double rootHeight = testTree.getRootHeight();
        assertEquals(7, rootHeight, 0.001);

        //v
        double vHeight = vertexV.getRootHeight();
        assertEquals(3, vHeight, 0.001);

        //u
        double uHeight = vertexU.getRootHeight();
        assertEquals(1, uHeight, 0.001);

        //leaf
        double leafHeight = leafA.getRootHeight();
        assertEquals(0, leafHeight, 0.001);
    }

    @Test
    public void copyConstructor() {
        LassoTree newTree = new LassoTree(testTree);
        newTree.setLength(7);
        assertNotEquals(newTree.getLength(), testTree.getLength(), 0.001);
    }

    @Test
    public void identifierConstructor() {
        Identifier id = new Identifier("Test", 1);
        LassoTree tree = new LassoTree(id);

        //Check taxon in tree is the same reference as one passed in
        assertEquals(tree.getTaxon(), id);
    }
}