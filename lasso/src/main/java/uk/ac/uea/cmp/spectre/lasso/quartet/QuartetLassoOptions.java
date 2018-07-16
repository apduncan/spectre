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

package uk.ac.uea.cmp.spectre.lasso.quartet;

import java.io.File;

public class QuartetLassoOptions {
    private File input;
    private File output;
    private boolean weighted;
    public static final boolean DEFAULT_WEIGHTED = true;
    public static final String DESC_OUTPUT = "File to write result to. The output is a nexus file containing a set of" +
            "quartets found in the input distance matrix, after any mising distances which can be inferred have been.";
    public static final String DESC_WEIGHTED = "Weighted or unweighted quartets, should be true or false.";

    public QuartetLassoOptions(File input, File output, boolean weighted) {
        this.input = input;
        this.output = output;
        this.weighted = weighted;
    }

    public QuartetLassoOptions() {
        this(null, null, QuartetLassoOptions.DEFAULT_WEIGHTED);
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

    public boolean isWeighted() {
        return weighted;
    }

    public void setWeighted(boolean weighted) {
        this.weighted = weighted;
    }
}
