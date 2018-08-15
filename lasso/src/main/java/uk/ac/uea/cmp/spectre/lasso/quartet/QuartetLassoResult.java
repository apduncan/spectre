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

import uk.ac.uea.cmp.spectre.core.ds.distance.DistanceMatrix;
import uk.ac.uea.cmp.spectre.core.io.nexus.NexusWriter;

import java.io.File;
import java.io.IOException;

public class QuartetLassoResult {
    private StringBuilder quartetBlock;
    private DistanceMatrix matrix;

    public QuartetLassoResult() {
        this.quartetBlock = null;
        this.matrix = null;
    }

    public QuartetLassoResult(StringBuilder quartetBlock, DistanceMatrix matrix) {
        this.quartetBlock = quartetBlock;
        this.matrix = matrix;
    }

    public StringBuilder getQuartetBlock() {
        return quartetBlock;
    }

    public void setQuartetBlock(StringBuilder quartetBlock) {
        this.quartetBlock = quartetBlock;
    }

    public DistanceMatrix getMatrix() {
        return matrix;
    }

    public void setMatrix(DistanceMatrix matrix) {
        this.matrix = matrix;
    }

    public void save(File file) throws IOException {
        //Write the file out in Nexus format
        NexusWriter writer = new NexusWriter();
        writer.appendHeader();
        writer.appendLine();
        writer.append(matrix.getTaxa());
        writer.appendLine();
        writer.append(this.quartetBlock.toString());
        writer.appendLine();
        writer.append(matrix);
        writer.write(file);
    }
}
