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

package uk.ac.uea.cmp.spectre.lasso.lasso;import org.apache.commons.lang3.tuple.Pair;
import uk.ac.uea.cmp.spectre.core.ds.Identifier;
import uk.ac.uea.cmp.spectre.core.ds.IdentifierList;
import uk.ac.uea.cmp.spectre.core.io.nexus.NexusWriter;
import uk.ac.uea.cmp.spectre.lasso.LassoTree;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class LassoResult {
    private List<LassoTree> trees;
    private Map<Pair<Identifier, Identifier>, Double> distancesUsed;

    public LassoResult(List<LassoTree> trees, Map<Pair<Identifier, Identifier>, Double> distancesUsed) {
        this.trees = trees;
        //ensure trees have all internal taxa removed
        this.trees.stream().forEach(tree -> tree.removeInternalIdentifier());
        this.distancesUsed = distancesUsed;
    }

    /**
     * Removes entries from distancesUsed where one or both taxa do not appear in the set taxa.
     * Modifies distancesUsed in place.
     * @param taxa The taxa set of the largest tree constructed
     */
    private Map<Pair<Identifier, Identifier>, Double> removeNonLeafTaxa(final Set<Identifier> taxa) {
        return this.distancesUsed.keySet().stream()
                .filter(pair -> (taxa.contains(pair.getLeft()) && taxa.contains(pair.getRight())))
                .collect(Collectors.toMap(key -> key, key -> this.distancesUsed.get(key)));
    }

    /**
     * Write the tree and corresponding strong lasso (if update methods permits) to disk, in Nexus format.
     * @param output File to write to
     * @throws IOException
     */
    public void save(File output) throws IOException {
        //Nexus does not write treeds, but does support writing custom blocks
        NexusWriter writer = new NexusWriter();
        //Make a list of all taxa used in any tree
        IdentifierList allTaxa = new IdentifierList();
        this.getTrees().stream()
                .flatMap(t -> t.findAllTaxa().stream())
                .distinct()
                .forEach(allTaxa::add);
        writer.appendHeader();
        writer.append(allTaxa);
        writer.appendLine("");
        //Write trees in newick format
        writer.appendLine("BEGIN TREES;");
        int treeNum = 1;
        for(LassoTree tree : this.getTrees()) {
            writer.appendLine("  TREE tree" + treeNum++ + " = " + tree.toString() + ";");
            //Writer the strong lasso in comments
            writer.appendLine("  [Strong Lasso shown below. Cord between taxa a and b with associated weight 4 written a -> b, 4]");
            this.getDistancesUsed(tree).entrySet().stream()
                    .map(entry -> "  [" + entry.getKey().getLeft().getName() + " -> " + entry.getKey().getRight().getName() + ", " + entry.getValue().toString() + "]")
                    .forEach(writer::appendLine);
        }
        writer.appendLine("END; [Trees]");
        writer.write(output);
    }

    public List<LassoTree> getTrees() {
        return this.trees;
    }

    public Map<Pair<Identifier, Identifier>, Double> getDistancesUsed(LassoTree tree) {
        return this.removeNonLeafTaxa(new HashSet<>(tree.findAllTaxa()));
    }

    /**
     * Return the size (number of taxa in) largest tree
     * @return The number of taxa in the tree with most taxa
     */
    public int sizeLargestTree() {
        return this.getTrees().stream()
                .max(Comparator.comparingInt(tree -> tree.findAllTaxa().size()))
                .get()
                .getNbTaxa();
    }
}
