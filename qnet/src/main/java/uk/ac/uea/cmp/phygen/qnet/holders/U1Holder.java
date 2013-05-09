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
package uk.ac.uea.cmp.phygen.qnet.holders;

import uk.ac.uea.cmp.phygen.core.ds.quartet.QuartetWeights;
import uk.ac.uea.cmp.phygen.core.ds.TaxonList;

import java.util.List;

public class U1Holder {

    public U1Holder(List<TaxonList> theLists, int N, QuartetWeights theQuartetWeights) {

        counts = new Integer[QuartetWeights.over2(N)];
        weights = new Double[QuartetWeights.over2(N)];

        for (int n = 0; n < QuartetWeights.over2(N); n++) {

            counts[n] = new Integer(0);
            weights[n] = new Double(0.0);

        }

        // now, we store everything properly

        for (int i = 1; i < N; i++) {

            for (int j = i + 1; j < N + 1; j++) {

                // for every pair i, j

                // find which list they are in
                // for those two indices
                // take all combinations of path entries
                // against all combinations of other paths by all other path entries

                int a = -1, b = -1;

                for (int m = 0; m < theLists.size(); m++) {

                    TaxonList tL = theLists.get(m);

                    if (tL.contains(i)) {

                        a = m;
                        break;

                    }

                }

                for (int m = 0; m < theLists.size(); m++) {

                    TaxonList tL = theLists.get(m);

                    if (tL.contains(j)) {

                        b = m;
                        break;

                    }

                }

                if (a == b) {

                    // if on the same path, no quartets meet the conditions

                    counts[QuartetWeights.over2(j - 1) + QuartetWeights.over1(i - 1)] = new Integer(0);
                    weights[QuartetWeights.over2(j - 1) + QuartetWeights.over1(i - 1)] = new Double(0.0);

                    continue;

                }

                // otherwise:
                // we now have the list indices

                int count = 0;
                double weight = 0.0;

                TaxonList A = theLists.get(a);
                TaxonList B = theLists.get(b);

                // we now have two non-same lists

                for (int xA1 = 0; xA1 < A.size() - 1; xA1++) {

                    for (int xA2 = xA1 + 1; xA2 < A.size(); xA2++) {

                        for (int xB1 = 0; xB1 < B.size() - 1; xB1++) {

                            for (int xB2 = xB1 + 1; xB2 < B.size(); xB2++) {

                                // this is a unique, suitable quartet

                                int yA1 = A.get(xA1);
                                int yA2 = A.get(xA2);
                                int yB1 = B.get(xB1);
                                int yB2 = B.get(xB2);

                                count++;
                                weight += theQuartetWeights.getWeight(yA1, yB2, yA2, yB1);

                                // hope this does not mean doing stuff twice

                                count++;
                                weight += theQuartetWeights.getWeight(yA2, yB1, yA1, yB2);

                            }

                        }

                    }

                }

                counts[QuartetWeights.over2(j - 1) + QuartetWeights.over1(i - 1)] = new Integer(count);
                weights[QuartetWeights.over2(j - 1) + QuartetWeights.over1(i - 1)] = new Double(weight);

            }

        }

    }

    public int getN(int i, int j) {

        int x = Math.max(i, j);
        int y = Math.min(i, j);

        return counts[QuartetWeights.over2(x - 1) + QuartetWeights.over1(y - 1)].intValue();

    }

    public void setN(int i, int j, int newN) {

        int x = Math.max(i, j);
        int y = Math.min(i, j);

        counts[QuartetWeights.over2(x - 1) + QuartetWeights.over1(y - 1)] = new Integer(newN);

    }

    public double getU(int i, int j) {

        int x = Math.max(i, j);
        int y = Math.min(i, j);

        return weights[QuartetWeights.over2(x - 1) + QuartetWeights.over1(y - 1)].doubleValue();

    }

    public void setU(int i, int j, double newU) {

        int x = Math.max(i, j);
        int y = Math.min(i, j);

        weights[QuartetWeights.over2(x - 1) + QuartetWeights.over1(y - 1)] = new Double(newU);

    }

    Integer[] counts;
    Double[] weights;
}