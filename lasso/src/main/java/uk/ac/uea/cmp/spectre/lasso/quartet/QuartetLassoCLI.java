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

public class QuartetLassoCLI {
    private static Logger log = LoggerFactory.getLogger(QuartetLasso.class);

    public static final String OPT_OUTPUT = "output_file";
    public static final String OPT_WEIGHTED = "weighted";

    public static Options createOptions() {

        // create Options object
        Options options = new Options();

        options.addOption(OptionBuilder.withArgName("output").withLongOpt(OPT_OUTPUT).hasArg()
                .withDescription(QuartetLassoOptions.DESC_OUTPUT).create("o"));

        options.addOption(OptionBuilder.withArgName("weight").withLongOpt(OPT_WEIGHTED).hasArg()
                .withDescription(QuartetLassoOptions.DESC_WEIGHTED).create("w"));

        options.addOption(CommandLineHelper.HELP_OPTION);

        return options;
    }


    public static void main(String[] args) {

        //TODO: CommandLine uk.ac.uea.cmp.spectre.rooted.rooted.RootedLasso description
        CommandLine commandLine = new CommandLineHelper().startApp(createOptions(), "quartetlasso [options] <distance_matrix_file>",
                "Find all possible quartets in a distance matrix, which can contain missing values. " +
                        "Any missing values which can be inferred are added to the matrix before quartets are " +
                        "generated. Where a missing distance could have multiple values, the first one encountered " +
                        "is selected.\n" +
                        "Input can be either nexus format file containing a distances block, or a phylip format distance matrix.", args);

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

            String prefix = "unrootedlasso";
            File output = new File("./rooted-output.nex");
            if (commandLine.hasOption(OPT_OUTPUT)) {
                output = new File(commandLine.getOptionValue(OPT_OUTPUT));
            }

            // Ensure output directory exists
            if (output.exists()) {
                //Should throw exception if file cannot  be created
                output.createNewFile();
            }

            File input = new File(commandLine.getArgs()[0]);
            QuartetLassoOptions options = new QuartetLassoOptions();
            //Set options
            options.setInput(input);
            options.setOutput(output);
            if(commandLine.hasOption(OPT_WEIGHTED)) {
                try {
                    options.setWeighted(Boolean.valueOf(commandLine.getOptionValue(OPT_WEIGHTED)));
                } catch (Exception e) {
                    throw new IOException("Invalid option for weight, should be true or false");
                }
            }

            // Run RootedLasso
            new QuartetLasso(options).run();

        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println(StringUtils.join(e.getStackTrace(), "\n"));
            System.exit(1);
        }
    }
}
