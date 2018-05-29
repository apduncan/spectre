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

import org.apache.commons.lang3.time.StopWatch;
import uk.ac.uea.cmp.spectre.core.ds.Identifier;
import uk.ac.uea.cmp.spectre.core.ds.distance.DistanceMatrix;
import uk.ac.uea.cmp.spectre.core.ds.distance.FlexibleDistanceMatrix;
import uk.ac.uea.cmp.spectre.core.ds.distance.RandomDistanceGenerator;
import uk.ac.uea.cmp.spectre.core.io.nexus.Nexus;
import uk.ac.uea.cmp.spectre.core.io.nexus.NexusWriter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

public class Test {
    public static void main(String[] args) {
        System.out.println("Long testing string here");
        String assign = "Separate string";
        DistanceMatrix fd = new RandomDistanceGenerator().generateDistances(100);
        Nexus nexus = new Nexus();
        nexus.setTaxa(fd.getTaxa());
        nexus.setDistanceMatrix(fd);
        try {
            new NexusWriter().writeDistanceMatrix(new File("/home/hal/Dropbox/Dissertation/random2.nex"), fd);
        } catch (IOException err) {
            System.out.println("failed");
        }

        //First try at a lasso method
        //double[][] matrix = new double[][] { {0, 2, 0, 4, 0}, {2, 0, 2, 2, 0}, {0, 2, 0, 2, 6}, {4, 2, 2, 0, 6}, {0, 0, 6, 6, 0} };
        //with disconnected component
        /*double[][] matrix = new double[][] { {0, 2, 0, 4, 0, 0, 0}, {2, 0, 2, 2, 0, 0, 0}, {0, 2, 0, 2, 6, 0, 0},
                {4, 2, 2, 0, 6, 0, 0}, {0, 0, 6, 6, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 2}, {0, 0, 0, 0, 0, 2, 0} };*/
        LassoDistanceGraph simpleGraph = new LassoDistanceGraph(new RandomDistanceGenerator().generateDistances(503));
        //make some objects
        DistanceUpdater modal = DistanceUpdaterFactory.MODAL.get(new LassoOptions());
        CliqueFinder heuristic = CliqueFinderFactory.HEURISTIC.get(new LassoOptions());
        long countEdges = 0;
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        do {
            //Identify clique
            //LassoDistanceGraph minWeight = new LassoDistanceGraph(simpleGraph);
            //minWeight.retainMinEdges();
            Set<Identifier> clique = heuristic.find(simpleGraph);
            simpleGraph.joinCluster(new ArrayList<>(clique), modal);
            //Count non-0 edges
            countEdges = simpleGraph.getMap().values().stream().filter(length -> length > 0).count();
        } while (countEdges > 0);
        stopWatch.stop();
        System.out.println(stopWatch.getTime());
        System.out.println(simpleGraph.getLargestCluster());
    }
}