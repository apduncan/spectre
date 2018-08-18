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
    public static final String DESC_OUTPUT = "File to write result to. The output is a tree metric. The tree can be " +
            "constructed using a method such as neighbour joining.";
    //No options available for this algorithm yet - more will be required as developed further

    public UnrootedLassoOptions(File input, File output) {
        this.input = input;
        this.output = output;
    }

    public UnrootedLassoOptions() {
        this(null, null);
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
}
