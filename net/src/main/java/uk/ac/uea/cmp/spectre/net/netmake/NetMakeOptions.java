/*
 * Suite of PhylogEnetiC Tools for Reticulate Evolution (SPECTRE)
 * Copyright (C) 2017  UEA School of Computing Sciences
 *
 * This program is free software: you can redistribute it and/or modify it under the term of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package uk.ac.uea.cmp.spectre.net.netmake;

import uk.ac.uea.cmp.spectre.core.ds.distance.DistanceCalculatorFactory;
import uk.ac.uea.cmp.spectre.core.ds.split.circular.ordering.CircularOrderingAlgorithms;
import uk.ac.uea.cmp.spectre.core.ds.split.circular.ordering.nm.weighting.Weightings;

import java.io.File;

public class NetMakeOptions {

    // Defaults
    public static final double DEFAULT_TREE_WEIGHT = 0.5;


    // Options descriptions
    public static final String DESC_INPUT = "The file containing the distance matrix to input (supported formats are Nexus, EMBOSS and Phylip).  Alternatively, input can be a multiple sequence alignment file in Nexus or FastA format.  MSAs will be converted to distance matricies by the specified distance calculator method.";

    public static final String DESC_OUTPUT_PREFIX = "The location and file prefix for output files.  Default: ./netmake";

    public static final String DESC_OUTPUT_NETWORK = "The location to write the output network.";

    public static final String DESC_OUTPUT_TREE = "The location to write the output tree (if applicable)";

    public static final String DESC_TREE_PARAM = "The weighting parameter passed to the chosen weighting algorithm. " +
            " Value must be between 0.0 and 1.0.  Default: " + DEFAULT_TREE_WEIGHT;

    public static final String DESC_CO_ALG = "The circular ordering algorithm to use: " + CircularOrderingAlgorithms.toListString() + ". Default: NEIGHBORNET";

    public static final String DESC_DRAW = "If selected, netmake will draw the circular split system, to create a network.  If the number of taxa is high (>50) this can take a significant amount of time relative to generating the circular split system.";

    public static final String DESC_DIST_CALC = "If running from MSAs, the distance matrix calculator to use: " + DistanceCalculatorFactory.toListString() + ". Default: " + DistanceCalculatorFactory.JUKES_CANTOR.name();

    public static final String DESC_WEIGHTINGS_1 = "For NETMAKE circular ordering algorithm, select 1st weighting type: " + Weightings.toListString() + ".  Required if circular algorithm is NETMAKE.  Default: TSP";

    public static final String DESC_WEIGHTINGS_2 = "For NETMAKE circular ordering algorithm, select 2nd weighting type: " + Weightings.toListString() + ". Default: NONE";


    private File input;
    private File outputNetwork;
    private File outputTree;
    private String weighting1;
    private String weighting2;
    private double treeParam;
    private String coAlg;
    private String dc;
    private boolean draw;


    public NetMakeOptions() {
        this(null, null, null, null, null, DEFAULT_TREE_WEIGHT, "NEIGHBORNET", DistanceCalculatorFactory.JUKES_CANTOR.name(), false);
    }

    public NetMakeOptions(File input, File outputNetwork, File outputTree, String weighting1, String weighting2, double treeParam, String coAlg, String dc, boolean draw) {

        // Validates that we have sensible input for the weightings
        if (weighting1 != null && !weighting1.isEmpty())
            Weightings.valueOf(weighting1);

        if (weighting2 != null && !weighting2.isEmpty())
            Weightings.valueOf(weighting2);

        this.input = input;
        this.outputNetwork = outputNetwork;
        this.outputTree = outputTree;
        this.weighting1 = weighting1;
        this.weighting2 = weighting2;
        this.treeParam = treeParam;
        this.coAlg = coAlg;
        this.dc = dc;
        this.draw = draw;
    }

    public File getInput() {
        return input;
    }

    public void setInput(File input) {
        this.input = input;
    }

    public File getOutputNetwork() {
        return outputNetwork;
    }

    public void setOutputNetwork(File outputNetwork) {
        this.outputNetwork = outputNetwork;
    }

    public File getOutputTree() {
        return outputTree;
    }

    public void setOutputTree(File outputTree) {
        this.outputTree = outputTree;
    }

    public String getWeighting1() {
        return weighting1;
    }

    public void setWeighting1(String weighting1) {
        this.weighting1 = weighting1;
    }

    public String getWeighting2() {
        return weighting2;
    }

    public void setWeighting2(String weighting2) {
        this.weighting2 = weighting2;
    }

    public double getTreeParam() {
        return treeParam;
    }

    public void setTreeParam(double treeParam) {
        this.treeParam = treeParam;
    }

    public String getCoAlg() {
        return coAlg;
    }

    public void setCoAlg(String coAlg) {
        this.coAlg = coAlg;
    }

    public String getDc() {
        return dc;
    }

    public void setDc(String dc) {
        this.dc = dc;
    }

    public boolean isDraw() {
        return draw;
    }

    public void setDraw(boolean draw) {
        this.draw = draw;
    }
}
