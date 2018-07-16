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

package uk.ac.uea.cmp.spectre.lasso.lasso;
import uk.ac.uea.cmp.spectre.lasso.lasso.CliqueFinderFactory;
import uk.ac.uea.cmp.spectre.lasso.lasso.DistanceUpdaterFactory;

import java.io.File;


public class LassoOptions {
    final public static int DEFAULT_LASSO_RUNS = 10;
    final public static int DEFAULT_CLIQUE_ATTEMPTS = 10;
    final public static DistanceUpdaterFactory DEFAULT_DISTANCE_UPDATER = DistanceUpdaterFactory.MODAL;
    final public static CliqueFinderFactory DEFAULT_CLIQUE_FINDER = CliqueFinderFactory.HEURISTIC;
    final public static String DESC_INPUT = "A Nexus or Phylip formatted file containing a distance matrix. Missing" +
            "values should be represented by ?.";
    final public static String DESC_OUTPUT = "File to write constructed trees to. Output will be in Nexus format.";
    final public static String DESC_LASSO_RUNS = "Number of times to run Lasso algorithm. The run which produces the " +
            "tree containing the greatest number of taxa is used as the final result.";
    final public static String DESC_CLIQUE_ATTEMPTS = "The number of times to attempt to find a maximal clique. Only " +
            "relevant when using the heuristic clique finding method.";
    final public static String DESC_DISTANCE_UPDATER = "Method of updating the distance matrix after joining clusters. " +
            "Values: MODAL";
    final public static String DESC_CLIQUE_FINDER = "Method of locating maximal cliques in graph. BRONKERBOSCH will " +
            "find maximal clique but can be slower, HEURISTIC grows n cliques, and selects largest.";

    private int cliqueAttempts;
    private int lassoRuns;
    private DistanceUpdaterFactory updater;
    private CliqueFinderFactory cliqueFinder;
    private File input;
    private File output;

    public LassoOptions() {
        //initialise with defaults
        this.cliqueAttempts = this.DEFAULT_CLIQUE_ATTEMPTS;
        this.lassoRuns = this.DEFAULT_LASSO_RUNS;
        this.updater = this.DEFAULT_DISTANCE_UPDATER;
        this.cliqueFinder = this.DEFAULT_CLIQUE_FINDER;
        this.input = null;
        this.output = null;
    }

    public LassoOptions(int lassoRuns, int cliqueAttempts, CliqueFinderFactory cliqueFinder,
                        DistanceUpdaterFactory updater, File input, File output) {
        this.cliqueAttempts = cliqueAttempts;
        this.lassoRuns = lassoRuns;
        this.cliqueFinder = cliqueFinder;
        this.updater = updater;
        this.input = input;
        this.output = output;
    }


    public int getCliqueAttempts() {
        return cliqueAttempts;
    }

    public void setCliqueAttempts(int cliqueAttempts) {
        this.cliqueAttempts = cliqueAttempts;
    }

    public int getLassoRuns() {
        return lassoRuns;
    }

    public void setLassoRuns(int lassoRuns) {
        this.lassoRuns = lassoRuns;
    }

    public DistanceUpdaterFactory getUpdater() {
        return updater;
    }

    public void setUpdater(DistanceUpdaterFactory updater) {
        this.updater = updater;
    }

    public CliqueFinderFactory getCliqueFinder() {
        return cliqueFinder;
    }

    public void setCliqueFinder(CliqueFinderFactory cliqueFinder) {
        this.cliqueFinder = cliqueFinder;
    }

    public File getInput() {
        return input;
    }

    public void setInput(File input) {
        this.input = input;
    }

    public File getOutput() {
        return output;
    }

    public void setOutput(File output) {
        this.output = output;
    }
}
