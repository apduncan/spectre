/*
 * Suite of PhylogEnetiC Tools for Reticulate Evolution (SPECTRE)
 * Copyright (C) 2017  UEA School of Computing Sciences
 *
 * This program is free software: you can redistribute it and/or modify it under the term of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package uk.ac.uea.cmp.spectre.flatnj;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.log4j.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.earlham.metaopt.Objective;
import uk.ac.earlham.metaopt.Optimiser;
import uk.ac.earlham.metaopt.OptimiserException;
import uk.ac.earlham.metaopt.OptimiserFactory;
import uk.ac.earlham.metaopt.external.JOptimizer;
import uk.ac.uea.cmp.spectre.core.ds.Sequences;
import uk.ac.uea.cmp.spectre.core.ds.IdentifierList;
import uk.ac.uea.cmp.spectre.core.ds.distance.DistanceMatrix;
import uk.ac.uea.cmp.spectre.core.ds.network.FlatNetwork;
import uk.ac.uea.cmp.spectre.core.ds.network.Network;
import uk.ac.uea.cmp.spectre.core.ds.network.Vertex;
import uk.ac.uea.cmp.spectre.core.ds.network.draw.AngleCalculatorMaximalArea;
import uk.ac.uea.cmp.spectre.core.ds.network.draw.CompatibleCorrector;
import uk.ac.uea.cmp.spectre.core.ds.network.draw.PermutationSequenceDraw;
import uk.ac.uea.cmp.spectre.core.ds.quad.quadruple.QuadrupleSystem;
import uk.ac.uea.cmp.spectre.core.ds.split.SplitSystem;
import uk.ac.uea.cmp.spectre.core.ds.split.flat.FlatSplitSystem;
import uk.ac.uea.cmp.spectre.core.ds.split.flat.FlatSplitSystemFinal;
import uk.ac.uea.cmp.spectre.core.ds.split.flat.PermutationSequence;
import uk.ac.uea.cmp.spectre.core.ds.split.flat.PermutationSequenceFactory;
import uk.ac.uea.cmp.spectre.core.io.fasta.FastaReader;
import uk.ac.uea.cmp.spectre.core.io.nexus.*;
import uk.ac.uea.cmp.spectre.core.ui.cli.CommandLineHelper;
import uk.ac.uea.cmp.spectre.core.ui.gui.RunnableTool;
import uk.ac.uea.cmp.spectre.core.ui.gui.StatusTrackerWithView;
import uk.ac.uea.cmp.spectre.core.util.LogConfig;
import uk.ac.uea.cmp.spectre.flatnj.tools.*;
import uk.ac.uea.cmp.spectre.flatnj.tools.NexusReader;

import java.io.File;
import java.io.IOException;

/**
 * FlatNJ (FlatNetJoining) is a program for computing split networks that allow
 * for interior vertices to be labeled while being (almost) planar.
 *
 * @author balvociute
 */
public class FlatNJ extends RunnableTool {

    private static Logger log = LoggerFactory.getLogger(FlatNJ.class);

    private FlatNJOptions options;

    public FlatNJ(FlatNJOptions options) {
        this(options, null);
    }

    public FlatNJ(FlatNJOptions options, StatusTrackerWithView tracker) {
        super(tracker);
        this.options = options;
    }

    private void notifyUser(String message) {
        log.info(message);
        this.trackerInitUnknownRuntime(message);
    }

    @Override
    public void run() {
        try {

            // Check we have something sensible to work with
            if (this.options == null) {
                throw new IOException("Must specify a valid set of parameters to control FlatNJ.");
            }

            File inFile = options.getInFile();
            File outFile = options.getOutputFile();

            if (inFile == null || !inFile.exists()) {
                throw new IOException("Must specify a valid input file.");
            }

            if (outFile == null || outFile.isDirectory()) {
                throw new IOException("Must specify a valid path for output file.");
            }

            // Print the validated options
            log.info("Recognised these options:\n\n" +
                    this.options.toString());



            // Get a shortcut to runtime object for checking memory usage
            Runtime rt = Runtime.getRuntime();

            // Start timing
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            log.info("Starting job");
            log.debug("FREE MEM - at start: " + rt.freeMemory());

            notifyUser("Loading input data from: " + inFile);

            this.continueRun();

            // Work out input file type
            String extension = FilenameUtils.getExtension(inFile.getName());

            IdentifierList taxa = null;
            Sequences sequences = null;
            DistanceMatrix distanceMatrix = null;
            Locations locations = null;
            FlatSplitSystem ss = null;
            QuadrupleSystem qs = null;

            if (extension.equalsIgnoreCase("fa") || extension.equalsIgnoreCase("faa") || extension.equalsIgnoreCase("fas") || extension.equalsIgnoreCase("fasta")) {
                sequences = readAlignment(inFile);
                taxa = new IdentifierList(sequences.getTaxaLabels());
                log.info("Extracted " + taxa.size() + " sequences");
            } else if (extension.equalsIgnoreCase("nex") || extension.equalsIgnoreCase("nexus") || extension.equalsIgnoreCase("4s")) {

                // Read taxa block regardless

                Nexus nexus = new uk.ac.uea.cmp.spectre.core.io.nexus.NexusReader().parse(inFile);

                taxa = nexus.getTaxa();

                if (taxa == null) {
                    throw new IOException("No labels for the taxa were indicated");
                }

                if (options.getBlock() == null) {
                    log.info("Nexus file provided as input but no nexus block specified by user.  Will use first suitable block found in nexus file");
                    // First check for existing quadruple system
                    qs = readQuadruples(inFile.getAbsolutePath());
                    if (qs != null) {
                        log.info("Detected and loaded Quadruples Block");
                    } else {
                        // Next check for location data
                        locations = readLocations(inFile);
                        if (locations != null) {
                            log.info("Detected and loaded Locations Block");
                        } else {
                            // Next check for split system
                            ss = readSplitSystem(inFile);
                            if (ss != null) {
                                log.info("Detected and loaded Split System Block containing " + ss.getnSplits() + " splits over " + ss.getnTaxa() + " taxa");
                            } else {
                                // Next look for MSA
                                sequences = nexus.getAlignments();
                                if (sequences != null) {
                                    log.info("Detected and loaded Sequences Block.  Found " + sequences.size() + " sequences");
                                } else {
                                    throw new IOException("Couldn't find a valid block in nexus file.");
                                }
                            }
                        }
                    }
                } else {
                    String blockLowerCase = options.getBlock().toLowerCase();

                    log.info("Searching for " + blockLowerCase + " in nexus file.");
                    boolean loaded = false;

                    if (blockLowerCase.contentEquals("data") || blockLowerCase.contentEquals("characters")) {
                        sequences = nexus.getAlignments();
                        loaded = true;
                    } else if (blockLowerCase.contentEquals("locations")) {
                        locations = readLocations(inFile);
                        loaded = true;
                    } else if (blockLowerCase.contentEquals("splits")) {
                        ss = readSplitSystem(inFile);
                        loaded = true;
                    } else if (blockLowerCase.contentEquals("quadruples")) {
                        qs = readQuadruples(inFile.getAbsolutePath());
                        loaded = true;
                    }

                    if (!loaded) {
                        throw new IOException("Couldn't loaded requested block from nexus file.");
                    }
                }
            }

            rt.gc();
            log.debug("FREE MEM - after loading input: " + rt.freeMemory());

            this.continueRun();

            // Compute the Quadruple system from alternate information if we didn't just load it from disk
            if (qs == null) {

                notifyUser("Computing system of 4-splits (quadruples)");

                QSFactory qsFactory = null;

                if (sequences != null) {
                    qsFactory = new QSFactoryAlignment(sequences, distanceMatrix);
                } else if (locations != null) {
                    qsFactory = new QSFactoryLocation(locations);
                } else if (ss != null) {
                    qsFactory = new QSFactorySplitSystem(ss);
                } else {
                    throw new IOException("No suitable data found to create quadruple system");
                }

                if (qsFactory == null) {
                    throw new IOException("Error creating quadruple system factory");
                }

                qs = qsFactory.computeQS(true);

                if (options.isSaveStages()) {

                    File quadFile = new File(outFile.getParentFile(), outFile.getName() + ".quads.nex");
                    log.info("Saving quadruples to: " + quadFile.getAbsolutePath());
                    Writer writer = new Writer();
                    writer.open(quadFile.getAbsolutePath());
                    writer.write(taxa);
                    writer.write(qs);
                    writer.close();
                }
            }

            qs.subtractMin();   //Subtract minimal weights. They will be added back when the network is computed.
            log.info("Computed " + qs.getnQuadruples() + " quadruples");

            rt.gc();
            log.debug("FREE MEM - after creating quadruples: " + rt.freeMemory());

            this.continueRun();

            notifyUser("Computing ordering");
            PermutationSequence ps = new PermutationSequenceFactory().computePermutationSequence(qs);

            rt.gc();
            log.debug("FREE MEM - after computing ordering: " + rt.freeMemory());

            this.continueRun();

            // Updates Permutation Sequence permutationSequence
            notifyUser("Weighting flat split system");
            new WeightCalculatorImpl(ps, qs).fitWeights(options.getOptimiser());

            rt.gc();
            log.debug("FREE MEM - after weighting: " + rt.freeMemory());

            this.continueRun();

            log.info("Filtering splits below threshold: " + options.getThreshold());
            ps.filterSplits(options.getThreshold());

            this.continueRun();

            notifyUser("Finalising splits system");
            ps.setTaxaNames(taxa.getNames());
            ss = new FlatSplitSystemFinal(ps);
            log.info("Split system contains " + ss.getnSplits() + " splits");
            //ss.setActive(ps.getActive());  // Do we want to reset this from active (extra trivial splits would have been added in the constructor)

            if (options.isSaveStages()) {
                File ssFile = new File(outFile.getParentFile(), outFile.getName() + ".splits.nex");
                log.info("Saving splits to: " + ssFile.getAbsolutePath());
                Writer writer = new Writer();
                writer.open(ssFile.getAbsolutePath());
                writer.write(taxa);
                writer.write(ss);
                writer.close();
            }

            this.continueRun();

            notifyUser("Computing network");
            PermutationSequenceDraw psDraw = new PermutationSequenceDraw(ps.getSequence(),
                    ps.getSwaps(),
                    ps.getWeights(),
                    ps.getActive(),
                    ps.getTrivial());

            this.continueRun();

            log.debug("Drawing split system");
            Vertex net = psDraw.drawSplitSystem(-1.0);

            this.continueRun();

            Network network = new FlatNetwork(net);
            notifyUser("Optimising network layout");
            net = net.optimiseLayout(psDraw, network);

            this.continueRun();

            log.info("Correcting compatible splits");
            CompatibleCorrector compatibleCorrectorPrecise = new CompatibleCorrector(new AngleCalculatorMaximalArea());
            compatibleCorrectorPrecise.addInnerTrivial(net, psDraw, network);

            if (!network.veryLongTrivial()) {
                log.debug("Correcting trivial splits");
                compatibleCorrectorPrecise.moveTrivial(net, 5, network);
            }

            if (options.isSaveStages()) {
                File netFile = new File(outFile.getParentFile(), outFile.getName() + ".network.nex");
                log.info("Saving network to: " + netFile.getAbsolutePath());
                Writer writer = new Writer();
                writer.open(netFile.getAbsolutePath());
                writer.write(taxa);
                writer.write((FlatNetwork) network, taxa);
                writer.close();
            }

            Writer writer = new Writer();
            writer.open(outFile.getAbsolutePath());
            writer.write(taxa);
            writer.write(ss);
            writer.write(net, ps.getnTaxa(), ps.getCompressed(), taxa); // Do we need to do this here?
            writer.close();

            log.info("Saving complete nexus file to: " + outFile.getAbsolutePath());
            this.trackerFinished(true);

            // Print run time on screen
            stopWatch.stop();
            log.info("Completed Successfully - Total run time: " + stopWatch.toString());

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            this.setError(e);
            this.trackerFinished(false);
        } finally {
            this.notifyListener();
        }
    }


    /**
     * Reads alignment from fasta file and initializes {@linkplain Sequences}
     * and {@linkplain uk.ac.uea.cmp.spectre.core.ds.IdentifierList} objects.
     *
     * @param fastaFile fasta file path.
     */
    protected Sequences readAlignment(File fastaFile) throws IOException {
        log.debug("Reading sequences");
        Sequences a = new FastaReader().readAlignment(fastaFile);
        if (a.getSequences().length == 0) {
            throw new IOException("Could not read sequence alignment from '" + fastaFile + "'");
        }
        return a;
    }

    /**
     * Reads MSAs from a nexus file character block {@linkplain Sequences}
     * and {@linkplain uk.ac.uea.cmp.spectre.core.ds.IdentifierList} objects.
     *
     * @param msaNexusFile nexus file containing character block.
     */
    protected Sequences readNexusAlignment(File msaNexusFile) throws IOException {
        log.debug("Reading sequences");
        Sequences a = new uk.ac.uea.cmp.spectre.core.io.nexus.NexusReader().readAlignment(msaNexusFile);
        if (a.getSequences().length == 0) {
            throw new IOException("Could not read sequence alignments from '" + msaNexusFile.getAbsolutePath() + "'");
        }
        return a;
    }

    /**
     * Reads character distance matrix from DISTANCES block in nexus distance
     * matrix file and initializes {@linkplain uk.ac.uea.cmp.spectre.core.ds.distance.FlexibleDistanceMatrix} object.
     *
     * @param distanceMatrixFile nexus file path.
     */
    protected DistanceMatrix readDistanceMatrix(File distanceMatrixFile) throws IOException {
        log.debug("Reading distance matrix");
        return new uk.ac.uea.cmp.spectre.core.io.nexus.NexusReader().readDistanceMatrix(distanceMatrixFile);
    }

    /**
     * Reads locations from LOCATIONS block in nexus input file and initializes
     * {@linkplain Locations} object.
     *
     * @param inFile nexus file path.
     * @return a {@linkplain String} array containing taxa names.
     */
    protected Locations readLocations(File inFile) {
        log.debug("Reading locations");
        Locations loc = (Locations) new NexusReaderLocations().readBlock(inFile.getAbsolutePath());
        return loc;
    }

    /**
     * Reads QUADRUPLES block and prints progress messages
     *
     * @param inFile input file
     */
    protected QuadrupleSystem readQuadruples(String inFile) throws IOException {
        log.debug("Reading quadruples");
        NexusReader reader = new NexusReaderQuadruples();
        QuadrupleSystem qs = (QuadrupleSystem) reader.readBlock(inFile);
        return qs;
    }

    /**
     * Reads SPLITS block and prints progress messages
     *
     * @param inFile input file
     */
    protected FlatSplitSystem readSplitSystem(File inFile) {
        log.debug("Reading splits");
        NexusReader reader = new NexusReaderSplits();
        return (FlatSplitSystem) reader.readBlock(inFile.getAbsolutePath());
    }

}
