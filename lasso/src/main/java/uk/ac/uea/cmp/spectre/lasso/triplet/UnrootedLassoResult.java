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

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import uk.ac.uea.cmp.spectre.core.ds.distance.DistanceMatrix;
import uk.ac.uea.cmp.spectre.core.io.nexus.NexusWriter;
import uk.ac.uea.cmp.spectre.lasso.LassoDistanceGraph;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UnrootedLassoResult {
    //Map of triplet cover consituting a strong lasso -> the tree metric lassoed
    private List<Pair<LassoDistanceGraph, DistanceMatrix>> results;

    public UnrootedLassoResult() {
        this.results = new ArrayList<>();
    }

    public void addResult(LassoDistanceGraph lasso, DistanceMatrix treeMetric) {
        this.results.add(new ImmutablePair<>(lasso, treeMetric));
    }

    public List<Pair<LassoDistanceGraph, DistanceMatrix>> getResults() {
        return this.results;
    }

    /**
     * Output each tree metric generated. Each file will have the same name suffixed by the
     * number of taxa in the tree, followed by the number in the sequence (eg tree metric 1 of 3).
     * @param file The file to output the tree metric(s) to. If multiple tree metrics found, will output mutliple file
     *             preserving this naming format.
     */
    public void save(File file) throws IOException {
        if(this.results.size() == 1) {
            //Write a single file
            writeResult(this.results.get(0), file);
        } else {
            //Write multiple files
            //iterate over all results
            final String prefix = file.toString().substring(file.toString().length() - 4);
            final String suffix = ".nex";
            int sequence = 1;
            for(Pair<LassoDistanceGraph, DistanceMatrix> result : this.results) {
                File suffixedFile = new File(prefix + "_" + result.getLeft().getTaxa().size() + "_" + sequence + suffix);
                writeResult(result, suffixedFile);
                sequence++;
            }
        }
    }

    private void writeResult(Pair<LassoDistanceGraph, DistanceMatrix> result, File file) throws IOException {
        NexusWriter writer = new NexusWriter();
        LassoDistanceGraph lasso = result.getLeft();
        DistanceMatrix treeMetric = result.getRight();
        writer.appendHeader();
        writer.append(treeMetric.getTaxa());
        writer.appendLine();
        writer.append(treeMetric);
        //Record the lasso as comments
        writer.append("[Below is the strong lasso for the tree metric given in Distances block]");
        lasso.getMap().entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .map(entry -> "[" + entry.getKey().getRight().getName() + " -> " + entry.getKey().getLeft().getName() +
                ", " + entry.getValue().toString())
                .forEach(s -> writer.append(s));
        writer.write(file);
    }
}
