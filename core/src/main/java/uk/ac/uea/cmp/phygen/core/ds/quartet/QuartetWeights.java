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
package uk.ac.uea.cmp.phygen.core.ds.quartet;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * QuartetWeights class. This is a list of triplets representing the weights between each quartet.
 */
public class QuartetWeights extends ArrayList<QuartetWeighting> {

    public QuartetWeights() {
        this(0);
    }

    public QuartetWeights(final int size) {
        super(size);

        for(int i = 0; i < size; i++) {
            this.add(new QuartetWeighting());
        }
    }

    /**
     * getWeight gets a weight
     */
    public double getWeight(Quartet quartet) {

        Quartet sortedQuartet = quartet.createSortedQuartet();

        QuartetWeighting w = this.get(sortedQuartet.getIndex());

        return sortedQuartet.selectWeight(quartet, w);
    }

    /**
     * setWeight sets three weights
     */
    public void setWeight(Quartet q, QuartetWeighting weights) {

        Quartet sorted = q.createSortedQuartet();

        QuartetWeighting w = sorted.selectWeighting(q, weights);

        this.set(q.getIndex(), w);
    }

    /**
     * setWeight sets a weight
     */
    public void incrementWeight(Quartet q, double increment) {
        this.setWeight(q, this.getWeight(q) + increment);
    }

    /**
     * setWeight sets a weight
     */
    public void setWeight(Quartet q, double newW) {

        Quartet sortedQuartet = q.createSortedQuartet();

        QuartetWeighting w = this.get(sortedQuartet.getIndex());

        sortedQuartet.updateWeighting(q, w, newW);

        this.set(sortedQuartet.getIndex(), w);
    }




    public void normalize(boolean useMax) {

        for (int n = 0; n < this.size(); n++) {
            this.get(n).normalise(false, useMax);
        }
    }

    public void logNormalize(boolean useMax) {

        for (int n = 0; n < this.size(); n++) {
            this.get(n).normalise(true, useMax);
        }
    }

    public QuartetWeights translate(List<String> taxonNamesOld, List<String> taxonNamesNew) {

        int NNew = taxonNamesNew.size();
        int NOld = taxonNamesOld.size();

        this.ensureCapacity(NNew);

        for (int iA = 0; iA < NOld - 3; iA++) {

            for (int iB = iA + 1; iB < NOld - 2; iB++) {

                for (int iC = iB + 1; iC < NOld - 1; iC++) {

                    for (int iD = iC + 1; iD < NOld; iD++) {

                        int a = iA + 1;
                        int b = iB + 1;
                        int c = iC + 1;
                        int d = iD + 1;

                        int nA = taxonNamesNew.indexOf(taxonNamesOld.get(iA)) + 1;
                        int nB = taxonNamesNew.indexOf(taxonNamesOld.get(iB)) + 1;
                        int nC = taxonNamesNew.indexOf(taxonNamesOld.get(iC)) + 1;
                        int nD = taxonNamesNew.indexOf(taxonNamesOld.get(iD)) + 1;

                        this.setWeight(new Quartet(nA, nB, nC, nD), getWeight(new Quartet(a, b, c, d)));
                        this.setWeight(new Quartet(nA, nC, nB, nD), getWeight(new Quartet(a, c, b, d)));
                        this.setWeight(new Quartet(nA, nD, nB, nC), getWeight(new Quartet(a, d, b, c)));
                    }
                }
            }
        }

        return this;
    }


    public MeanSumResult meanSum(List<Integer> aL, List<Integer> bL, List<Integer> cL, List<Integer> dL) {

        int count = 0;
        double score = 0;

        for (int a = 0; a < aL.size(); a++) {
            for (int b = 0; b < bL.size(); b++) {
                for (int c = 0; c < cL.size(); c++) {
                    for (int d = 0; d < dL.size(); d++) {

                        int x = aL.get(a);
                        int y = bL.get(b);
                        int u = cL.get(c);
                        int v = dL.get(d);

                        score += this.getWeight(new Quartet(x, y, u, v));

                        count++;
                    }
                }
            }
        }

        return new MeanSumResult(count, score);
    }


    public static class MeanSumResult {
        private int count;
        private double score;

        public MeanSumResult(final int count, final double score) {
            this.count = count;
            this.score = score;
        }

        public int getCount() {
            return count;
        }

        public double getScore() {
            return score;
        }
    }


    public void divide(QuartetWeights summer) {

        for (int n = 0; n < this.size(); n++) {
            this.get(n).divide(summer.get(n));
        }
    }

    /**
     * note: this now simply computes a weighted sum. This is the part that may be done in any number of ways here,
     * take weighted sum of every quartet where the quartet is nonzero
     */
    public void add(QuartetWeights aW, double w) {

        for (int n = 0; n < this.size(); n++) {
            this.get(n).weightedSum(aW.get(n), w);
        }
    }

    /**
     * So... we go through taxonNames, which is the metalist check every quartet defined for it take the taxonList for
     * the objects in loader, and their corresponding weights if a quartet is defined for that list, add its weight to
     * the corresponding summer position summer must have been translated according to the metalist
     * @param taxonNames
     * @param metaTaxonNames
     * @param weights
     */
    public void sum(List<String> taxonNames, List<List<String>> metaTaxonNames, List<Double> weights) {

        for(int i = 0; i < metaTaxonNames.size(); i++) {

            double w = weights.get(i);
            List<String> lesserNames = metaTaxonNames.get(i);

            // course through all quartets of taxonNames
            // if taxonNames (quartet entries) are contained in lesserNames
            // add w to summer (quartet)

            int N = taxonNames.size();

            for (int a = 0; a < N - 3; a++) {
                for (int b = a + 1; b < N - 2; b++) {
                    for (int c = b + 1; c < N - 1; c++) {
                        for (int d = c + 1; d < N; d++) {
                            String sA = taxonNames.get(a);
                            String sB = taxonNames.get(b);
                            String sC = taxonNames.get(c);
                            String sD = taxonNames.get(d);

                            if (lesserNames.contains(sA)
                                    && lesserNames.contains(sB)
                                    && lesserNames.contains(sC)
                                    && lesserNames.contains(sD)) {

                                Quartet q = new Quartet(a + 1, b + 1, c + 1, d + 1);

                                double oldW = this.getWeight(q);

                                this.setWeight(q, new QuartetWeighting(oldW + w, oldW + w, oldW + w));
                            }
                        }
                    }
                }
            }
        }
    }
}