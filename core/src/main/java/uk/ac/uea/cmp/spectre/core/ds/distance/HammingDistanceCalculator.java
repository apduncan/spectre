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

package uk.ac.uea.cmp.spectre.core.ds.distance;

/**
 * Created with IntelliJ IDEA.
 * User: Dan
 * Date: 01/05/13
 * Time: 01:19
 * To change this template use File | Settings | File Templates.
 */
public class HammingDistanceCalculator extends AbstractDistanceMatrixCalculator {

    public static int hammingCount(String s1, String s2) {
        if (s1.length() != s2.length()) {
            throw new IllegalArgumentException("Sequence " + s1 + " and sequence " + s2 + " are different lengths.");
        }

        int diffs = 0;
        for (int k = 0; k < s1.length(); k++) {
            if (s1.charAt(k) != s2.charAt(k)) {
                diffs++;
            }
        }

        return diffs;
    }

    @Override
    protected double calculateDistance(String s1, String s2) {

        return (double)hammingCount(s1, s2) / (double)s1.length();
    }
}
