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

package uk.ac.uea.cmp.spectre.lasso.lasso;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.uea.cmp.spectre.core.ui.cli.CommandLineHelper;
import uk.ac.uea.cmp.spectre.core.util.LogConfig;

import java.io.File;
import java.io.IOException;

public class LassoCLI {
    private static Logger log = LoggerFactory.getLogger(LassoCLI.class);

    public static final String OPT_OUTPUT = "output_file";
    public static final String OPT_LASSO_RUNS= "lasso_runs";
    public static final String OPT_CLIQUE_ATTEMPTS = "clique_attempts";
    public static final String OPT_DISTANCE_UPDATER = "distance_updater";
    public static final String OPT_CLIQUE_FINDER = "clique_finder";

    public static Options createOptions() {

        // create Options object
        Options options = new Options();

        options.addOption(OptionBuilder.withArgName("output").withLongOpt(OPT_OUTPUT).hasArg()
                .withDescription(LassoOptions.DESC_OUTPUT).create("o"));

        options.addOption(OptionBuilder.withArgName("runs").withLongOpt(OPT_LASSO_RUNS).hasArg()
                .withDescription(LassoOptions.DESC_LASSO_RUNS).create("r"));

        options.addOption(OptionBuilder.withArgName("cattempts").withLongOpt(OPT_CLIQUE_ATTEMPTS).hasArg()
                .withDescription(LassoOptions.DESC_CLIQUE_ATTEMPTS).create("a"));

        options.addOption(OptionBuilder.withLongOpt(OPT_DISTANCE_UPDATER).hasArg()
                .withDescription(LassoOptions.DESC_DISTANCE_UPDATER).create("du"));

        options.addOption(OptionBuilder.withLongOpt(OPT_CLIQUE_FINDER).hasArg()
                .withDescription(LassoOptions.DESC_CLIQUE_FINDER).create("cf"));

        options.addOption(CommandLineHelper.HELP_OPTION);

        return options;
    }


    public static void main(String[] args) {

        CommandLine commandLine = new CommandLineHelper().startApp(createOptions(),
                "netmake [options] <distance_matrix_file>",
                "Creates an equidistant tree from a distance matrix. The input matrix can have missing" +
                        "values. The tree is created from a subset of the input distances, where this subset has only " +
                        "one possible tree representation.\n" +
                        "Input can be either nexus format file containing a distances block, or a phylip format " +
                        "distance matrix.", args);

        // If we didn't return a command line object then just return.  Probably the user requested help or
        // input invalid args
        if (commandLine == null) {
            return;
        }

        try {

            LogConfig.defaultConfig();
            log.info("Parsing arguments");

            if (commandLine.getArgs().length == 0) {
                throw new IOException("No input file specified.");
            }
            else if (commandLine.getArgs().length > 1) {
                throw new IOException("Only expected a single input file.");
            }

            String prefix = "netmake";
            File output = new File("./lasso-output.nex");
            if (commandLine.hasOption(OPT_OUTPUT)) {
                output = new File(commandLine.getOptionValue(OPT_OUTPUT));
            }

            // Ensure output directory exists
            if (output.exists()) {
                //Should throw exception if file cannot  be created
                output.createNewFile();
            }

            File input = new File(commandLine.getArgs()[0]);
            LassoOptions options = new LassoOptions();
            //Set options
            options.setInput(input);
            options.setOutput(output);
            if(commandLine.hasOption(OPT_LASSO_RUNS))
                options.setLassoRuns(Integer.parseInt(commandLine.getOptionValue(OPT_LASSO_RUNS)));
            if(commandLine.hasOption(OPT_CLIQUE_ATTEMPTS))
                options.setCliqueAttempts(Integer.parseInt(commandLine.getOptionValue(OPT_CLIQUE_ATTEMPTS)));
            if(commandLine.hasOption(OPT_CLIQUE_FINDER)) {
                try {
                    CliqueFinderFactory finder = CliqueFinderFactory.valueOf(commandLine.getOptionValue(OPT_CLIQUE_FINDER).toUpperCase());
                    options.setCliqueFinder(finder);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Invalid clique finder method");
                }
            }
            if(commandLine.hasOption(OPT_DISTANCE_UPDATER)) {
                try {
                    DistanceUpdaterFactory updater = DistanceUpdaterFactory.valueOf(commandLine.getOptionValue(OPT_DISTANCE_UPDATER).toUpperCase());
                    options.setUpdater(updater);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Invalid distance update method");
                }
            }

            // Run Lasso
            new Lasso(options).run();

        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println(StringUtils.join(e.getStackTrace(), "\n"));
            System.exit(1);
        }
    }
}
