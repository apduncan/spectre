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

import java.io.File;

public class UnrootedLassoOptions {
    private File input;
    private File output;
    private ChordalSubgraphFinder.SEED_TREE seedTree;
    public static final String DESC_OUTPUT = "File to write result to. The output is a tree metric. The tree can be " +
            "constructed using a method such as neighbour joining. If more than one tree is created, will output a " +
            "tree metric for each in a separate file.";
    public static final String DESC_SEED_TREE = "BREADTH or DEPTH. Method of build the spanning tree which is used as a starting point " +
            "to build a chordal subgrapgh. Uses either a breadth or depth first search.";

    public UnrootedLassoOptions(File input, File output, ChordalSubgraphFinder.SEED_TREE seedTree) {
        this.input = input;
        this.output = output;
        this.seedTree = seedTree;
    }

    public UnrootedLassoOptions() {
        this(null, null, ChordalSubgraphFinder.SEED_DEFAULT);
    }

    public UnrootedLassoOptions(File input, File output) {
        this(input, output, ChordalSubgraphFinder.SEED_DEFAULT);
    }

    public File getOutput() {
        return output;
    }

    public void setOutput(File output) {
        this.output = output;
    }

    public File getInput() {
        return input;
    }

    public void setInput(File input) {
        this.input = input;
    }

    public ChordalSubgraphFinder.SEED_TREE getSeedTree() {
        return seedTree;
    }

    public void setSeedTree(ChordalSubgraphFinder.SEED_TREE seedTree) {
        this.seedTree = seedTree;
    }
}
