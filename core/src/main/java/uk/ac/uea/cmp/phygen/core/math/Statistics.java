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

package uk.ac.uea.cmp.phygen.core.math;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Dan
 * Date: 27/04/13
 * Time: 16:01
 * To change this template use File | Settings | File Templates.
 */
public class Statistics {

    public static double sumDoubles(List<Double> list) {
        double sum = 0.0;

        for (Double val : list) {
            sum += val.doubleValue();
        }

        return sum;
    }

    public static double sumDoubles(double[] array) {
        double sum = 0.0;

        for (int i = 0; i < array.length; i++) {
            sum += array[i];
        }

        return sum;
    }


    public static double sumIntegers(List<Integer> list) {
        int sum = 0;

        for (Integer val : list) {
            sum += val.intValue();
        }

        return sum;
    }

    public static double sumIntegers(int[] array) {
        int sum = 0;

        for (int i = 0; i < array.length; i++) {
            sum += array[i];
        }

        return sum;
    }

    public static long sumLongs(List<Long> list) {

        long sum = 0L;

        for (Long val : list) {
            sum += val.longValue();
        }

        return sum;
    }

    public static double sumLongs(long[] array) {
        long sum = 0L;

        for (int i = 0; i < array.length; i++) {
            sum += array[i];
        }

        return sum;
    }
}
