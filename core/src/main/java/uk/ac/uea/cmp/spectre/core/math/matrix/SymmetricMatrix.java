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

package uk.ac.uea.cmp.spectre.core.math.matrix;

import uk.ac.uea.cmp.spectre.core.ds.quad.quartet.QuartetUtils;

/**
 * Created by IntelliJ IDEA. User: Analysis Date: 2004-jul-11 Time: 19:08:50 To
 * change this template use Options | File Templates.
 */
public class SymmetricMatrix {

    private int size;
    private double[] diagonal;
    private double[] triangle;

    public SymmetricMatrix(int size) {

        this.size = size;

        diagonal = new double[size];
        triangle = new double[QuartetUtils.over2(size)];

        for (int n = 0; n < size; n++) {
            diagonal[n] = 0.0;
        }

        for (int n = 0; n < QuartetUtils.over2(size); n++) {
            triangle[n] = 0.0;
        }

    }

    public int getSize() {
        return size;
    }

    public void setElementAt(int i, int j, double newW) {

        if (i > j) {
            triangle[QuartetUtils.over2(i) + QuartetUtils.over1(j)] = newW;
        } else if (j > i) {
            triangle[QuartetUtils.over2(j) + QuartetUtils.over1(i)] = newW;
        } else {
            diagonal[i] = newW;
        }
    }

    public double getElementAt(int i, int j) {

        return i > j ?
                triangle[QuartetUtils.over2(i) + QuartetUtils.over1(j)] :
                j > i ?
                        triangle[QuartetUtils.over2(j) + QuartetUtils.over1(i)] :
                        diagonal[i];

    }


    public double[][] toArray() {
        double[][] w = new double[getSize()][getSize()];

        for (int i = 0; i < getSize(); i++) {
            for (int j = 0; j < getSize(); j++) {
                w[i][j] = getElementAt(i, j);
            }
        }

        return w;
    }
}
