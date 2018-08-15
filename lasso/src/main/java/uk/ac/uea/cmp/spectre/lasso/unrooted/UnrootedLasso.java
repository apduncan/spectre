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

package uk.ac.uea.cmp.spectre.lasso.unrooted;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.uea.cmp.spectre.core.ds.Identifier;
import uk.ac.uea.cmp.spectre.core.ds.distance.DistanceMatrix;
import uk.ac.uea.cmp.spectre.core.ds.distance.FlexibleDistanceMatrix;
import uk.ac.uea.cmp.spectre.core.io.SpectreReader;
import uk.ac.uea.cmp.spectre.core.io.SpectreReaderFactory;
import uk.ac.uea.cmp.spectre.core.io.nexus.Nexus;
import uk.ac.uea.cmp.spectre.core.io.nexus.NexusReader;
import uk.ac.uea.cmp.spectre.core.ui.gui.RunnableTool;
import uk.ac.uea.cmp.spectre.core.ui.gui.StatusTracker;
import uk.ac.uea.cmp.spectre.lasso.LassoDistanceGraph;
import uk.ac.uea.cmp.spectre.lasso.LassoQuartets;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

public class UnrootedLasso extends RunnableTool {
    private UnrootedLassoOptions options;
    private static Logger logger = LoggerFactory.getLogger(UnrootedLasso.class);

    public UnrootedLasso(UnrootedLassoOptions options) {
        this.options = options;
    }

    public UnrootedLasso(UnrootedLassoOptions options, StatusTracker tracker) {
        super(tracker);
        this.options = options;
    }

    public UnrootedLasso() {
        this(new UnrootedLassoOptions());
    }

    public void setOptions(UnrootedLassoOptions options) {
        this.options = options;
    }

    public UnrootedLassoOptions getOptions() {
        return this.options;
    }

    private void notifyUser(String message) {
        logger.info(message);
        this.trackerInitUnknownRuntime(message);
    }

    @Override
    public void run() {
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
            if (reader.getIdentifier() == "NEXUS") {
                Nexus nexus = new NexusReader().parse(this.options.getInput());
                matrix = nexus.getDistanceMatrix();
            } else {
                matrix = reader.readDistanceMatrix(this.options.getInput());
            }

            if (matrix == null)
                throw new IOException("Could not find distance matrix in input");

            this.notifyUser("Loaded distance matrix containing " + matrix.size() + " taxa");
            this.notifyUser("Executing Unrooted Lasso");
            UnrootedLassoResult result = this.execute(matrix);
            this.notifyUser("Saving results to disk");
            result.save(this.options.getOutput());
            stopwatch.stop();
            this.notifyUser("Completed - Run time: " + stopwatch.toString());
            this.trackerFinished(true);
        } catch (Exception err) {
            this.notifyUser(err.toString());
            this.setError(err);
        } finally {
            this.notifyListener();
        }
    }

    private UnrootedLassoResult execute(DistanceMatrix input) {
        UnrootedLassoResult result = new UnrootedLassoResult();
        this.notifyUser("Locating triplet cover in input");
        LassoDistanceGraph cover = new TripletCoverBuilder(new LassoDistanceGraph(input)).find();
        this.notifyUser("Triplet cover for " + cover.size() + " taxa located");
        //Shell this cover to a tree metric
        this.notifyUser("Shelling triplet cover to tree metric");
        DistanceMatrix treeMetric = new LassoQuartets(cover).enrichMatrix();
        result.addResult(cover, treeMetric);
        return result;
    }
}
