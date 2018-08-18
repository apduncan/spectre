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

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import uk.ac.uea.cmp.spectre.core.ds.Identifier;
import uk.ac.uea.cmp.spectre.core.ds.distance.DistanceMatrix;
import uk.ac.uea.cmp.spectre.core.io.nexus.NexusWriter;
import uk.ac.uea.cmp.spectre.lasso.LassoDistanceGraph;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class UnrootedLassoResult {
    //Map of quartet cover consituting a strong rooted -> the tree metric lassoed
    private DistanceMatrix tree;
    private Set<Pair<Identifier, Identifier>> lasso;

    public UnrootedLassoResult() {
        this.tree = null;
        this.lasso = null;
    }

    /**
     * Output each tree metric generated. Each file will have the same name suffixed by the
     * number of taxa in the tree, followed by the number in the sequence (eg tree metric 1 of 3).
     * @param file The file to output the tree metric(s) to. If multiple tree metrics found, will output mutliple file
     *             preserving this naming format.
     */
    public void save(File file) throws IOException {
        if(this.tree == null || this.lasso == null)
            throw new NullPointerException("No result to write");
        NexusWriter writer = new NexusWriter();
        writer.appendHeader();
        writer.append(this.tree.getTaxa());
        writer.appendLine();
        writer.append(this.tree);
        //Record the rooted as comments
        writer.append("[Below is the strong rooted for the tree metric given in Distances block]");
        this.lasso.stream()
                .map(entry -> "[" + entry.getRight().getName() + " -> " + entry.getLeft().getName() +
                ", " + this.tree.getDistance(entry.getRight().getName(), entry.getLeft().getName()) + "]")
                .forEach(s -> writer.appendLine(s));
        writer.write(file);
    }

    public DistanceMatrix getTree() {
        return tree;
    }

    public void setTree(DistanceMatrix tree) {
        this.tree = tree;
    }

    public Set<Pair<Identifier, Identifier>> getLasso() {
        return lasso;
    }

    public void setLasso(Set<Pair<Identifier, Identifier>> lasso) {
        this.lasso = lasso;
    }
}
