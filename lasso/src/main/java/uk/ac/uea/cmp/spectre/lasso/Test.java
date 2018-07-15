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

package uk.ac.uea.cmp.spectre.lasso;
import org.apache.commons.lang3.tuple.Pair;
import uk.ac.uea.cmp.spectre.core.ds.Identifier;
import uk.ac.uea.cmp.spectre.core.ds.distance.RandomDistanceGenerator;
import uk.ac.uea.cmp.spectre.lasso.lasso.CliqueFinder;
import uk.ac.uea.cmp.spectre.lasso.lasso.CliqueFinderFactory;
import uk.ac.uea.cmp.spectre.lasso.lasso.LassoOptions;

import java.util.*;

public class Test {
    public static void cliqueAccuracyTest() {
        final int missingFrom = 0;
        final int missingTo = 50;
        final int sizeFrom = 20;
        final int sizeTo = 500;
        final int missingStep = 10;
        final int sizeStep = 20;
        final int repeat = 100;
        final RandomDistanceGenerator generator = new RandomDistanceGenerator();
        CliqueFinder heuristic = CliqueFinderFactory.HEURISTIC.get(new LassoOptions());
        CliqueFinder exhaustive = CliqueFinderFactory.BRONKERBOSCH.get(new LassoOptions());

        //Loop through missing percentages
        for(int missing = missingFrom; missing <= missingTo; missing += missingStep) {
            for(int size = sizeFrom; size <= sizeTo; size += sizeStep) {
                for(int rep = 0; rep < repeat; rep++) {
                    //Generate random distance matrix
                    LassoDistanceGraph graph = new LassoDistanceGraph(generator.generateDistances(size));
                    //Delete distances
                    deleteEdges(missing, graph);
                    //Find max clique using exhaustive
                    Set<Identifier> exhaustiveClique = exhaustive.find(graph);
                    //Find max clique using heuristic
                    Set<Identifier> heuristicClique = heuristic.find(graph);
                    boolean match = exhaustiveClique.size() == heuristicClique.size();
                    int cliqueSize = exhaustiveClique.size();
                    //Output
                    System.out.println(String.join(",", new String[]{
                            String.valueOf(missing),
                            String.valueOf(size),
                            String.valueOf(match),
                            String.valueOf(cliqueSize)
                    }));
                }
            }
        }
    }

    public static void deleteEdges(int pcDelete, LassoDistanceGraph graph) {
        int numToDelete = graph.getMap().size() * (pcDelete / 100);
        List<Pair<Identifier, Identifier>> keySet = new ArrayList<>(graph.getMap().keySet());
        Collections.shuffle(keySet);
        //Select first numToDelete and delete
        for(int i = 0; i < numToDelete; i++) {
            graph.removeDistance(keySet.get(i));
        }
    }
    public static void main(String[] args) {
        cliqueAccuracyTest();
/*        System.out.println("Long testing string here");
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
        *//*double[][] matrix = new double[][] { {0, 2, 0, 4, 0, 0, 0}, {2, 0, 2, 2, 0, 0, 0}, {0, 2, 0, 2, 6, 0, 0},
                {4, 2, 2, 0, 6, 0, 0}, {0, 0, 6, 6, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 2}, {0, 0, 0, 0, 0, 2, 0} };*//*
        uk.ac.uea.cmp.spectre.lasso.LassoDistanceGraph simpleGraph = new uk.ac.uea.cmp.spectre.lasso.LassoDistanceGraph(new RandomDistanceGenerator().generateDistances(503));
        //make some objects
        uk.ac.uea.cmp.spectre.lasso.lasso.DistanceUpdater modal = uk.ac.uea.cmp.spectre.lasso.lasso.DistanceUpdaterFactory.MODAL.get(new uk.ac.uea.cmp.spectre.lasso.lasso.LassoOptions());
        uk.ac.uea.cmp.spectre.lasso.lasso.CliqueFinder heuristic = uk.ac.uea.cmp.spectre.lasso.lasso.CliqueFinderFactory.HEURISTIC.get(new uk.ac.uea.cmp.spectre.lasso.lasso.LassoOptions());
        long countEdges = 0;
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        do {
            //Identify clique
            //uk.ac.uea.cmp.spectre.lasso.LassoDistanceGraph minWeight = new uk.ac.uea.cmp.spectre.lasso.LassoDistanceGraph(simpleGraph);
            //minWeight.retainMinEdges();
            Set<Identifier> clique = heuristic.find(simpleGraph);
            simpleGraph.joinCluster(new ArrayList<>(clique), modal);
            //Count non-0 edges
            countEdges = simpleGraph.getMap().values().stream().filter(length -> length > 0).count();
        } while (countEdges > 0);
        stopWatch.stop();
        System.out.println(stopWatch.getTime());
        System.out.println(simpleGraph.getLargestCluster());*/

        //write a random file to use
//        DistanceMatrix fd = new RandomDistanceGenerator().generateDistances(100);
//        double[][] reducedMatrix = new double[][] { {0, 4}, {4, 0} };
//        double[][] matrix = new double[][] { {0, 2, 2, 0, 0, 0}, {2, 0, 2, 0, 0, 0}, {2, 2, 0, 6, 0, 0}, {0, 0, 6, 0, 4, 4}, {0, 0, 0, 0, 4, 0, 4}, {0, 0, 0, 4, 4, 0} };
//        double[][] modalProb = new double[][] { {0, 2, 2, 0 }, {2, 0, 2, 0}, {2, 2, 0, 2}, {0, 0, 2, 0} };
//        LassoDistanceGraph lg = new LassoDistanceGraph(new FlexibleDistanceMatrix(modalProb));
////        lg.setDistance("D", "E", 4);
//        Nexus nexus = new Nexus();
//        nexus.setTaxa(lg.getTaxa());
//        nexus.setDistanceMatrix(lg);
//        try {
//            new NexusWriter().writeDistanceMatrix(new File("/home/hal/Dropbox/Dissertation/spectre/spectre/lasso/src/test/resources/random100.nex"), fd);
//        } catch (IOException err) {
//            System.out.println("failed");
//        }
//
//        BasicConfigurator.configure();
//        LogManager.getRootLogger().setLevel(Level.INFO);
//        LassoOptions options = new LassoOptions();
//        options.setLassoRuns(2);
//        options.setInput(new File("/home/hal/Dropbox/Dissertation/random503.nex"));
//        options.setOutput(new File("/home/hal//Dropbox/Dissertation/outputtest.nex"));
//        Lasso lasso = new Lasso(options);
//        lasso.run();

        //Generate nexus file for graph from paper, adapted to have only one solution
//        double[][] exampleMatrix = {{0, 2, 0, 4, 0}, {2, 0, 2, 2, 6}, {0, 2, 0, 2, 6}, {4, 2, 2, 0, 8}, {0, 6, 6, 8, 0}};
//        FlexibleDistanceMatrix exampleMat = new FlexibleDistanceMatrix(exampleMatrix);
//        Nexus nexus = new Nexus();
//        nexus.setDistanceMatrix(exampleMat);
//        try {
//            new NexusWriter().writeDistanceMatrix(new File("/home/hal/Dropbox/Dissertation/example-mod.nex"), exampleMat);
//        } catch(IOException e) {
//            System.out.println("Fail write");
//        }
        //Generate nexus file for two separate components test
//        double[][] exampleMatrix = {{0, 1, 0, 0, 0}, {1, 0, 0, 0, 0}, {0, 0, 0, 1, 1}, {0, 0, 1, 0, 1}, {0, 0, 1, 1, 0}};
//        FlexibleDistanceMatrix exampleMat = new FlexibleDistanceMatrix(exampleMatrix);
//        Nexus nexus = new Nexus();
//        nexus.setDistanceMatrix(exampleMat);
//        try {
//            new NexusWriter().writeDistanceMatrix(new File("/home/hal/Dropbox/Dissertation/spectre/spectre/lasso/src/test/resources/disconnected.nex"), exampleMat);
//        } catch(IOException e) {
//            System.out.println("Fail write");
//        }

    }
}