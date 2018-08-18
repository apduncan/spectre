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

package uk.ac.uea.cmp.spectre.lasso.quartet;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.uea.cmp.spectre.core.ds.distance.DistanceMatrix;
import uk.ac.uea.cmp.spectre.core.io.SpectreReader;
import uk.ac.uea.cmp.spectre.core.io.SpectreReaderFactory;
import uk.ac.uea.cmp.spectre.core.io.nexus.Nexus;
import uk.ac.uea.cmp.spectre.core.io.nexus.NexusReader;
import uk.ac.uea.cmp.spectre.core.ui.gui.RunnableTool;
import uk.ac.uea.cmp.spectre.core.ui.gui.StatusTracker;
import uk.ac.uea.cmp.spectre.lasso.LassoShelling;

import java.io.IOException;

public class QuartetLasso extends RunnableTool {
    private QuartetLassoOptions options;
    private static Logger logger = LoggerFactory.getLogger(QuartetLasso.class);

    public QuartetLasso() {
        this.options = new QuartetLassoOptions();
    }

    public QuartetLasso(QuartetLassoOptions options) {
        this.options = options;
    }

    public QuartetLasso(QuartetLassoOptions options, StatusTracker tracker) {
        super(tracker);
        this.options = options;
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
            this.notifyUser("Executing Quartet RootedLasso");
            QuartetLassoResult result = this.execute(matrix);
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

    private QuartetLassoResult execute(DistanceMatrix input) {
        //Infer any missing distances which are possible
        this.notifyUser("Inferring missing distances");
        DistanceMatrix shelled = new LassoShelling(input).altShell();
        this.notifyUser("Generating quartet block");
        StringBuilder quartetBlock = new LassoShelling(shelled).getQuartetsAsString(this.getOptions().isWeighted());
        return new QuartetLassoResult(quartetBlock, shelled);
    }

    private void notifyUser(String message) {
        logger.info(message);
        this.trackerInitUnknownRuntime(message);
    }

    public QuartetLassoOptions getOptions() {
        return options;
    }

    public void setOptions(QuartetLassoOptions options) {
        this.options = options;
    }
}
