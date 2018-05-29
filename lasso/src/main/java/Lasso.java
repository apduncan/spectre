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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.uea.cmp.spectre.core.ds.Identifier;
import uk.ac.uea.cmp.spectre.core.ds.distance.DistanceMatrix;
import uk.ac.uea.cmp.spectre.core.io.SpectreReader;
import uk.ac.uea.cmp.spectre.core.io.SpectreReaderFactory;
import uk.ac.uea.cmp.spectre.core.io.nexus.Nexus;
import uk.ac.uea.cmp.spectre.core.io.nexus.NexusReader;
import uk.ac.uea.cmp.spectre.core.ui.gui.RunnableTool;
import uk.ac.uea.cmp.spectre.core.ui.gui.StatusTracker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class Lasso extends RunnableTool {
    private static Logger logger = LoggerFactory.getLogger(Lasso.class);
    private LassoOptions options;

    public Lasso() {
        this(new LassoOptions());
    }

    public Lasso(LassoOptions options) {
        this.options = options;
    }

    public Lasso(LassoOptions options, StatusTracker tracker) {
        super(tracker);
        this.options = options;
    }

    public LassoOptions getOptions() {
        return options;
    }

    public void setOptions(LassoOptions options) {
        this.options = options;
    }

    private void notifyUser(String message) {
        logger.info(message);
        this.trackerInitUnknownRuntime(message);
    }

    @Override
    public void run() {
        //Run lasso using the current set options
        try {
            //Timing
            StopWatch stopwatch = new StopWatch();
            stopwatch.start();

            //Read from files
            //Code based on NetMake module
            this.notifyUser("Loading distance matrix from:" + this.getOptions().getInput().getAbsolutePath());
            SpectreReaderFactory factory = SpectreReaderFactory.getInstance();
            SpectreReader reader = factory.create(FilenameUtils.getExtension(this.options.getInput().getName()));

            DistanceMatrix matrix = null;
            //Nexus format has different method for reading distance matrices
            if(reader.getIdentifier() == "NEXUS") {
                Nexus nexus = new NexusReader().parse(this.options.getInput());
                matrix = nexus.getDistanceMatrix();
            } else {
                matrix = reader.readDistanceMatrix(this.options.getInput());
            }

            if(matrix == null)
                throw new IOException("Could not find distance matrix in input");

            logger.info("Loaded distance matrix containing " + matrix.size() + " taxa");
            this.notifyUser("Executing Lasso");
            LassoResult result = this.execute(matrix);
            this.notifyUser("Saving results to disk");
            result.save(this.options.getOutput());
            stopwatch.stop();
            logger.info("Completed - Run time: " + stopwatch.toString());

        } catch(Exception e) {
            logger.error(e.getMessage());
            this.setError(e);
            this.trackerFinished(false);
        } finally {
            this.notifyListener();
        }
    }

    private LassoResult execute(DistanceMatrix matrix) {
        List<LassoResult> results = new ArrayList<>();
        //Instantiate clique finder and distance updater objects based on options
        DistanceUpdater updater = this.getOptions().getUpdater().get(this.getOptions());
        CliqueFinder finder = this.getOptions().getCliqueFinder().get(this.getOptions());
        //Run user defined number of times, then select result with most taxa
        for(int i = 0; i < this.getOptions().getLassoRuns(); i++) {
            this.notifyUser("Executing Lasso run " + (i+1) + " of " + this.getOptions().getLassoRuns());
            //Make a new copy of the graph from the input matrix
            LassoDistanceGraph graph = new LassoDistanceGraph(matrix);
            long countEdges = 0;
            do {
                Set<Identifier> clique = finder.find(graph);
                graph.joinCluster(new ArrayList<>(clique), updater);
                countEdges = graph.getMap().values().stream().filter(length -> length > 0).count();
            } while(countEdges > 0);
            //TODO: distancesUsed need to go into result
            results.add(new LassoResult(graph.getLargestCluster(), null));
        }
        //Return result with most taxa
        return results.parallelStream().min(Comparator.comparingInt(a -> a.getTree().getNbTaxa())).get();
    }
}
