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

import uk.ac.uea.cmp.phygen.core.math.tuple.Triplet;

import java.util.LinkedList;

/**
 * QuartetWeights class
 */
public class QuartetWeights {

    /**
     * ArrayList of ArrayLists of etc. of triplets
     * <p/>
     * and a get and set method
     */
    public static QuartetWeights qW;

    public QuartetWeights() {

        theSize = 0;

    }

    /**
     * Local overs
     */
    public static int over4(int n) {

        if (n > 4) {

            return n * (n - 1) * (n - 2) * (n - 3) / 24;

        } else if (n == 4) {

            return 1;

        } else {

            return 0;

        }

    }

    public static int over3(int n) {

        if (n > 3) {

            return n * (n - 1) * (n - 2) / 6;

        } else if (n == 3) {

            return 1;

        } else {

            return 0;

        }

    }

    public static int over2(int n) {

        if (n > 2) {

            return n * (n - 1) / 2;

        } else if (n == 2) {

            return 1;

        } else {

            return 0;

        }

    }

    public static int over1(int n) {

        if (n > 1) {

            return n;

        } else if (n == 1) {

            return 1;

        } else {

            return 0;

        }

    }

    /**
     * Check if filled...
     */
    public int getSize() {

        return theSize;

    }

    /**
     * Ensure capacity...
     */
    public void ensureCapacity(int N) {

        data = new Triplet[over4(N)];

    }

    public void initialize() {

        int N = data.length;

        for (int n = 0; n < N; n++) {

            Triplet t = new Triplet<Double>(0.0, 0.0, 0.0);
            data[n] = t;
        }

    }

    public void setSize(int theNewSize) {

        theSize = theNewSize;

    }

    /**
     * getWeight gets a weight
     */
    public double getWeight(int a, int b, int c, int d) {

        /**
         *
         * Size-order and decide which topology this is
         *
         * Access the lists, return the value there
         *
         */
        /**
         *
         * Create size-ordered quadruple x, y, u, v from a, b, c, d
         *
         */
        int x = a;
        int y = b;
        int u = c;
        int v = d;
        int m;

        if (y > x) {

            m = x;
            x = y;
            y = m;

        }

        if (u > x) {

            m = x;
            x = u;
            u = m;

        }

        if (v > x) {

            m = x;
            x = v;
            v = m;

        }

        if (u > y) {

            m = y;
            y = u;
            u = m;

        }

        if (v > y) {

            m = y;
            y = v;
            v = m;

        }

        if (v > u) {

            m = u;
            u = v;
            v = m;

        }

        /**
         *
         * Next, use size-ordered numbers to access the triple
         *
         */
        Triplet w = data[over4(x - 1) + over3(y - 1) + over2(u - 1) + over1(v - 1)];

        /**
         *
         * Now, determine which quartet to take. Use the unordered numbers, they
         * match one ordering or other
         *
         */
        /**
         *
         * Result variable
         *
         */
        double result = 0.0;

        /**
         *
         * If either first or last pair is first or last pair of the stored one,
         * use first weight
         *
         */
        if (((a == x || a == y) && (b == x || b == y)) || ((c == x || c == y) && (d == x || d == y))) {

            result = w.getA().doubleValue();

        } /**
         *
         * Next possibility
         *
         */
        else if (((a == x || a == u) && (b == x || b == u)) || ((c == x || c == u) && (d == x || d == u))) {

            result = w.getB().doubleValue();

        } /**
         *
         * Third possibility
         *
         */
        else if (((a == x || a == v) && (b == x || b == v)) || ((c == x || c == v) && (d == x || d == v))) {

            result = w.getC().doubleValue();

        }

        return result;

    }

    /**
     * setWeight sets three weights
     */
    public void setWeight(int a, int b, int c, int d, double w1, double w2, double w3) {

        /**
         *
         * Size-order, find which is which
         *
         */
        /**
         *
         * Create size-ordered quadruple x, y, u, v from a, b, c, d
         *
         */
        int x = a;
        int y = b;
        int u = c;
        int v = d;
        int m;

        if (y > x) {

            m = x;
            x = y;
            y = m;

        }

        if (u > x) {

            m = x;
            x = u;
            u = m;

        }

        if (v > x) {

            m = x;
            x = v;
            v = m;

        }

        if (u > y) {

            m = y;
            y = u;
            u = m;

        }

        if (v > y) {

            m = y;
            y = v;
            v = m;

        }

        if (v > u) {

            m = u;
            u = v;
            v = m;

        }

        /**
         *
         * Investigate which topology of a, b, c, d that the topologies of x, y,
         * u, v correspond to, and set weights accordingly
         *
         */
        /**
         *
         * See if xy|uv is ab|cd (w1), ac|bd (w2), or ad|bc (w3)
         *
         */
        /**
         *
         * The first weight of the stored triplet
         *
         */
        double r1 = 0;

        if (((x == a || x == b) && (y == a || y == b)) || ((u == a || u == b) && (v == a || v == b))) {

            r1 = w1;

        } else if (((x == a || x == c) && (y == a || y == c)) || ((u == a || u == c) && (v == a || v == c))) {

            r1 = w2;

        } else if (((x == a || x == d) && (y == a || y == d)) || ((u == a || u == d) && (v == a || v == d))) {

            r1 = w3;

        }

        /**
         *
         * See if xu|yv is ab|cd (w1), ac|bd (w2), or ad|bc (w3)
         *
         */
        /**
         *
         * The second weight of the stored triplet
         *
         */
        double r2 = 0;

        if (((x == a || x == b) && (u == a || u == b)) || ((y == a || y == b) && (v == a || v == b))) {

            r2 = w1;

        } else if (((x == a || x == c) && (u == a || u == c)) || ((y == a || y == c) && (v == a || v == c))) {

            r2 = w2;

        } else if (((x == a || x == d) && (u == a || u == d)) || ((y == a || y == d) && (v == a || v == d))) {

            r2 = w3;

        }

        /**
         *
         * See if xv|uy is ab|cd (w1), ac|bd (w2), or ad|bc (w3)
         *
         */
        /**
         *
         * The third weight of the stored triplet
         *
         */
        double r3 = 0;

        if (((x == a || x == b) && (v == a || v == b)) || ((u == a || u == b) && (y == a || y == b))) {

            r3 = w1;

        } else if (((x == a || x == c) && (v == a || v == c)) || ((u == a || u == c) && (y == a || y == c))) {

            r3 = w2;

        } else if (((x == a || x == d) && (v == a || v == d)) || ((u == a || u == d) && (y == a || y == d))) {

            r3 = w3;

        }

        Triplet w = new Triplet<Double>(r1, r2, r3);

        /**
         *
         * Set to w
         *
         */
        data[over4(x - 1) + over3(y - 1) + over2(u - 1) + over1(v - 1)] = w;

        theSize++;

    }

    /**
     * setWeight sets a weight
     */
    public void setWeight(int a, int b, int c, int d, double newW) {

        /**
         *
         * Size-order, find which is which
         *
         */
        /**
         *
         * Create size-ordered quadruple x, y, u, v from a, b, c, d
         *
         */
        //System.out.println("set weight of quartet: " + a + " " + b + " " + c + " " + d + " : " + data.length);
        int x = a;
        int y = b;
        int u = c;
        int v = d;
        int m;

        if (y > x) {

            m = x;
            x = y;
            y = m;

        }

        if (u > x) {

            m = x;
            x = u;
            u = m;

        }

        if (v > x) {

            m = x;
            x = v;
            v = m;

        }

        if (u > y) {

            m = y;
            y = u;
            u = m;

        }

        if (v > y) {

            m = y;
            y = v;
            v = m;

        }

        if (v > u) {

            m = u;
            u = v;
            v = m;

        }
//try{
        Triplet w = data[over4(x - 1) + over3(y - 1) + over2(u - 1) + over1(v - 1)];

        /**
         *
         * Investigate which topology of a, b, c, d that the topologies of x, y,
         * u, v correspond to, and set weights accordingly
         *
         */
        /**
         *
         * See if xy|uv is ab|cd (w1), ac|bd (w2), or ad|bc (w3)
         *
         */
        /**
         *
         * The first weight of the stored triplet
         *
         */
        if (((x == a || x == b) && (y == a || y == b)) || ((u == a || u == b) && (v == a || v == b))) {

            w.setA(newW);

        } else if (((x == a || x == b) && (u == a || u == b)) || ((y == a || y == b) && (v == a || v == b))) {

            w.setB(newW);

        } else if (((x == a || x == b) && (v == a || v == b)) || ((u == a || u == b) && (y == a || y == b))) {

            w.setC(newW);

        }

        data[over4(x - 1) + over3(y - 1) + over2(u - 1) + over1(v - 1)] = w;
    }
    /*
     * catch(Exception e){ e.printStackTrace(); }
     *
     * }
     *
     * /**
     *
     * Normalizing
     *
     */

    public void normalize(boolean useMax) {

        if (useMax) {

            for (int n = 0; n < data.length; n++) {

                Triplet<Double> t = data[n];

                double min = Math.min(t.getA(), Math.min(t.getB(), t.getC()));

                double q1 = t.getA() - min;
                double q2 = t.getB() - min;
                double q3 = t.getC() - min;

//                q1 = q1 / (q1 + q2 + q3);
//                q2 = q2 / (q1 + q2 + q3);
//                q3 = q3 / (q1 + q2 + q3);

                t.setA(q1);
                t.setB(q2);
                t.setC(q3);

                data[n] = t;

            }

        } else {

            for (int n = 0; n < data.length; n++) {

                Triplet<Double> t = data[n];

                double max = Math.max(t.getA(), Math.max(t.getB(), t.getC()));

                double q1 = max - t.getA();
                double q2 = max - t.getB();
                double q3 = max - t.getC();

//                q1 = q1 / (q1 + q2 + q3);
//                q2 = q2 / (q1 + q2 + q3);
//                q3 = q3 / (q1 + q2 + q3);

                t.setA(q1);
                t.setB(q2);
                t.setC(q3);

                data[n] = t;

            }

        }

    }

    /**
     * Log-Normalizing
     */
    public void logNormalize(boolean useMax) {

        if (useMax) {

            for (int n = 0; n < data.length; n++) {

                Triplet t = data[n];

                // lowest weight must be nonnegative

                double q1 = t.getA().doubleValue();
                double q2 = t.getB().doubleValue();
                double q3 = t.getC().doubleValue();

                if (q1 < 0.0) {

                    System.out.println("Error: Quartet file contains negative weight!");

                    System.exit(1);

                } else if (q1 == 0.0) {

                    q1 = -15.0;

                } else {

                    q1 = Math.log(q1);

                }

                if (q2 < 0.0) {

                    System.out.println("Error: Quartet file contains negative weight!");

                    System.exit(1);

                } else if (q2 == 0.0) {

                    q2 = -15.0;

                } else {

                    q2 = Math.log(q2);

                }

                if (q3 < 0.0) {

                    System.out.println("Error: Quartet file contains negative weight!");

                    System.exit(1);

                } else if (q3 == 0.0) {

                    q3 = -15.0;

                } else {

                    q3 = Math.log(q3);

                }

                double min = Math.min(Math.min(q1, q2), q3);

                t.setA(q1 - min);
                t.setB(q2 - min);
                t.setC(q3 - min);

                data[n] = t;

            }

        } else {

            for (int n = 0; n < data.length; n++) {

                Triplet t = data[n];

                double q1 = t.getA().doubleValue();
                double q2 = t.getB().doubleValue();
                double q3 = t.getC().doubleValue();

                if (q1 < 0.0) {

                    System.out.println("Error: Quartet file contains negative weight!");

                    System.exit(1);

                } else if (q1 == 0.0) {

                    q1 = -15.0;

                } else {

                    q1 = Math.log(q1);

                }

                if (q2 < 0.0) {

                    System.out.println("Error: Quartet file contains negative weight!");

                    System.exit(1);

                } else if (q2 == 0.0) {

                    q2 = -15.0;

                } else {

                    q2 = Math.log(q2);

                }

                if (q3 < 0.0) {

                    System.out.println("Error: Quartet file contains negative weight!");

                    System.exit(1);

                } else if (q3 == 0.0) {

                    q3 = -15.0;

                } else {

                    q3 = Math.log(q3);

                }

                double max = Math.max(Math.max(q1, q2), q3);

                t.setA(max - q1);
                t.setB(max - q2);
                t.setC(max - q3);

                data[n] = t;

            }

        }

    }

    public Triplet[] getData() {

        return data;

    }

    public void setData(Triplet[] newData) {

        data = newData;

    }

    public QuartetWeights translate(LinkedList taxonNamesOld, LinkedList taxonNamesNew) {

        int NNew = taxonNamesNew.size();
        int NOld = taxonNamesOld.size();

        qW = new QuartetWeights();
        qW.ensureCapacity(NNew);
        qW.initialize();

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

                        qW.setWeight(nA, nB, nC, nD, getWeight(a, b, c, d));
                        qW.setWeight(nA, nC, nB, nD, getWeight(a, c, b, d));
                        qW.setWeight(nA, nD, nB, nC, getWeight(a, d, b, c));

                    }

                }

            }

        }

        return qW;

    }

    /**
     * The heart of the data structure
     */
    Triplet[] data;
    /**
     * Number of actual quartets
     */
    int theSize;
}