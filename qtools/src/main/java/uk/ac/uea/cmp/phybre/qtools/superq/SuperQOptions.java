/*
 * Phylogenetics Tool suite
 * Copyright (C) 2013  UEA CMP Phylogenetics Group
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package uk.ac.uea.cmp.phybre.qtools.superq;

import uk.ac.tgac.metaopt.Objective;
import uk.ac.tgac.metaopt.Optimiser;
import uk.ac.tgac.metaopt.OptimiserException;
import uk.ac.tgac.metaopt.OptimiserFactory;
import uk.ac.uea.cmp.phybre.qtools.superq.problems.SecondaryProblem;
import uk.ac.uea.cmp.phybre.qtools.superq.problems.SecondaryProblemFactory;

import java.io.File;


public class SuperQOptions {

    public static final String DESC_SCALING_SOLVER = "If a scaling optimiser is selected, the input trees are scaled.  Available optimisers: " +
            OptimiserFactory.getInstance().listOperationalOptimisersAsString(Objective.ObjectiveType.QUADRATIC);

    public static final String DESC_PRIMARY_SOLVER = "The primary optimiser to use with Non-Negative Least Square (NNLS) objective.  " +
            "If internal solver is selected or no valid solver is specified SuperQ will use its own internal NNLS solver.  " +
            "Otherwise the user can specify an external solver to solve the NNLS objective.  Available external solvers: " +
            OptimiserFactory.getInstance().listOperationalOptimisersAsString(Objective.ObjectiveType.QUADRATIC);

    public static final String DESC_SECONDARY_SOLVER = "The primary optimiser occasionally may not return a unique solution.  You can guarantee " +
            "a unique solution by using a secondary solver.  Available Solvers: " +
            OptimiserFactory.getInstance().listOperationalOptimisersAsString();

    public static final String DESC_SECONDARY_OBJECTIVE = "The objective to use with the secondary optimiser.  " +
            "Available options: " + SecondaryProblemFactory.getInstance().listSecondaryObjectivesAsString();

    public static final String DESC_OUTPUT = "REQUIRED: The output file containing the split systems generated by SuperQ";

    public static final String DESC_FILTER = "The filter value to use.  Default: no filter (0.0)";

    private File[] inputFiles;
    private File outputFile;
    private Optimiser scalingSolver;
    private Optimiser primarySolver;
    private Optimiser secondarySolver;
    private SecondaryProblem secondaryProblem;
    private Double filter;
    private boolean verbose;

    public SuperQOptions() throws OptimiserException {
        this(null, null,
                null, null, null, null,
                null, false);
    }

    public SuperQOptions(File[] inputFiles, File outputFile,
                         Optimiser scalingSolver, Optimiser primarySolver, Optimiser secondarySolver, SecondaryProblem secondaryProblem,
                         Double filter, boolean verbose) {

        this.inputFiles = inputFiles;
        this.outputFile = outputFile;
        this.scalingSolver = scalingSolver;
        this.primarySolver = primarySolver;
        this.secondaryProblem = secondaryProblem;
        this.secondarySolver = secondarySolver;
        this.filter = filter;
        this.verbose = verbose;
    }

    public File[] getInputFiles() {
        return inputFiles;
    }

    public void setInputFiles(File[] inputFiles) {
        this.inputFiles = inputFiles;
    }

    public SecondaryProblem getSecondaryProblem() {
        return secondaryProblem;
    }

    public void setSecondaryProblem(SecondaryProblem secondaryProblem) {
        this.secondaryProblem = secondaryProblem;
    }

    public Double getFilter() {
        return filter;
    }

    public void setFilter(Double filter) {
        this.filter = filter;
    }

    public Optimiser getScalingSolver() {
        return scalingSolver;
    }

    public void setScalingSolver(Optimiser scalingSolver) {
        this.scalingSolver = scalingSolver;
    }

    public Optimiser getPrimarySolver() {
        return primarySolver;
    }

    public void setPrimarySolver(Optimiser primarySolver) {
        this.primarySolver = primarySolver;
    }

    public Optimiser getSecondarySolver() {
        return secondarySolver;
    }

    public void setSecondarySolver(Optimiser secondarySolver) {
        this.secondarySolver = secondarySolver;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
}