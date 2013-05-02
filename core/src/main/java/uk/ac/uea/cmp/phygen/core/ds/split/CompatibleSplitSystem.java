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
package uk.ac.uea.cmp.phygen.core.ds.split;

import uk.ac.uea.cmp.phygen.core.alg.CircularNNLS;
import uk.ac.uea.cmp.phygen.core.ds.distance.DistanceMatrix;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Dan
 * Date: 28/04/13
 * Time: 16:57
 * To change this template use File | Settings | File Templates.
 */
public class CompatibleSplitSystem extends CircularSplitSystem {

    public CompatibleSplitSystem(List<Split> splits, DistanceMatrix distanceMatrix, CircularOrdering circularOrdering) {

        super(splits, circularOrdering);

        if (circularOrdering.size() != distanceMatrix.size()) {
            throw new IllegalArgumentException("Distance matrix and circular ordering are not the same size");
        }

        this.setTaxa(distanceMatrix.getTaxaSet());

        this.setSplitWeights(this.calculateSplitWeighting(distanceMatrix, circularOrdering));

        reweight(this.getSplitWeights());
    }

    public CompatibleSplitSystem(CompatibleSplitSystem unweightedSplitSystem, TreeSplitWeights treeWeights) {

        super(unweightedSplitSystem.copySplits(), unweightedSplitSystem.getCircularOrdering().copy());

        this.setTaxa(unweightedSplitSystem.getTaxa().clone());

        reweight(treeWeights);
    }

    public TreeSplitWeights getTreeSplitWeights() {
        return (TreeSplitWeights) this.getSplitWeights();
    }


    protected void reweight(SplitWeights treeWeights) {

        int n = this.getNbTaxa();
        this.getSplits().clear();

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (treeWeights.getAt(j, i) != 0.0) {

                    ArrayList<Integer> sb = new ArrayList<>();
                    for (int k = i + 1; k < j + 1; k++) {
                        sb.add(this.getCircularOrdering().getAt(k));
                    }

                    this.addSplit(new Split(new SplitBlock(sb), n, treeWeights.getAt(j, i)));
                }
            }
        }
    }

    /**
     * Returns weightings for the edges of a specified tree.
     *
     * @return tree edge weightings
     */
    @Override
    public SplitWeights calculateSplitWeighting(DistanceMatrix distanceMatrix, CircularOrdering circularOrdering) {

        int n = distanceMatrix.size();
        double[][] treeWeights = new double[n][n];
        double[][] permutedDistances = new double[n][n];
        boolean[][] flag = new boolean[n][n];
        int[] permutationInvert = new int[n];

        for (int i = 0; i < n; i++) {
            permutationInvert[circularOrdering.getAt(i)] = i;
        }

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                flag[i][j] = false;
            }
        }

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                treeWeights[i][j] = 0.;
            }
        }

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                permutedDistances[i][j] = distanceMatrix.getDistance(circularOrdering.getAt(i), circularOrdering.getAt(j));
            }
        }

        for (int i = 0; i < this.getSplits().size(); i++) {

            SplitBlock sb = this.getSplitAt(i).getASide();

            int k = permutationInvert[sb.get(0)];
            int l = permutationInvert[sb.get(sb.size() - 1)];

            if (k == 0) {
                flag[n - 1][l] = true;
            } else {
                if ((l < n - 1) && (k > l)) {
                    flag[k - 1][l] = true;
                } else {
                    flag[l][k - 1] = true;
                }
            }
        }

//        assert(checkFlags(flag) == 17);


        new CircularNNLS().treeInCycleLeastSquares(permutedDistances, flag,
                n, treeWeights);

        //      assert(checkWeights(treeWeights) == 17);

        return new TreeSplitWeights(treeWeights);
    }

    private int checkFlags(boolean[][] flag) {

        int count = 0;
        for (int i = 0; i < flag.length; i++) {
            for (int j = 0; j < flag[i].length; j++) {
                if (flag[i][j] == true) {
                    count++;
                }
            }
        }

        return count;
    }

    private int checkWeights(double[][] weights) {

        int count = 0;
        for (int i = 0; i < weights.length; i++) {
            for (int j = 0; j < weights[i].length; j++) {
                if (weights[i][j] != 0.0) {
                    count++;
                }
            }
        }

        return count;
    }


}