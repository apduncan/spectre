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

package uk.ac.uea.cmp.spectre.lasso.triplet;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import uk.ac.uea.cmp.spectre.core.ds.Identifier;
import uk.ac.uea.cmp.spectre.core.ds.distance.DistanceMatrix;
import uk.ac.uea.cmp.spectre.core.ds.distance.FlexibleDistanceMatrix;
import uk.ac.uea.cmp.spectre.core.ds.distance.RandomDistanceGenerator;
import uk.ac.uea.cmp.spectre.lasso.DistanceUpdater;
import uk.ac.uea.cmp.spectre.lasso.LassoDistanceGraph;

import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class TripletCoverFinderTest {

    @Test
    public void findTripletCovers() {
        LassoDistanceGraph lg = new LassoDistanceGraph(new RandomDistanceGenerator().generateDistances(5));
        TripletCoverFinder tc = new TripletCoverFinder(lg);
        List<LassoDistanceGraph> covers = tc.findTripletCovers();
        System.out.println(covers.size());
        double[][] matrix = {{0, 1, 1, 0, 0}, {1, 0, 1, 0, 0}, {1, 1, 0, 1, 1}, {0, 0, 1, 0, 1}, {0, 0, 1, 1, 0}};
        lg = new LassoDistanceGraph(new FlexibleDistanceMatrix(matrix));
        tc = new TripletCoverFinder(lg);
        covers = tc.findTripletCovers();
        System.out.println(covers.size());
        System.out.println("Find in big random graph");
        lg = new LassoDistanceGraph(new RandomDistanceGenerator().generateDistances(300));
        //Delete some random edges
        List<Pair<Identifier, Identifier>> edges = lg.getMap().keySet().stream().collect(Collectors.toList());
        Collections.shuffle(edges);
        for(int i = 0; i < 5000; i++) {
            Pair<Identifier, Identifier> edge = edges.get(i);
            lg.removeDistance(edge);
        }
        //Wait for keypress
//        Scanner scanner = new Scanner(System.in);
//        System.out.println("Press to begin");
//        scanner.nextLine();
//        System.out.println("Begun");
        tc = new TripletCoverFinder(lg);
        covers = tc.findTripletCovers();
        System.out.println(covers.size());
    }
}