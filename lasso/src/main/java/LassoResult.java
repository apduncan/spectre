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

import org.apache.commons.lang3.tuple.Pair;
import uk.ac.uea.cmp.spectre.core.ds.Identifier;
import uk.ac.uea.cmp.spectre.core.ds.IdentifierList;
import uk.ac.uea.cmp.spectre.core.ds.tree.newick.NewickTree;
import uk.ac.uea.cmp.spectre.core.io.nexus.Nexus;
import uk.ac.uea.cmp.spectre.core.io.nexus.NexusWriter;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class LassoResult {
    private LassoTree tree;
    private Map<Pair<Identifier, Identifier>, Double> distancesUsed;

    public LassoResult(LassoTree tree, Map<Pair<Identifier, Identifier>, Double> distancesUsed) {
        this.tree = tree;
        tree.removeInternalIdentifier();
        distancesUsed = this.removeNonLeafTaxa(distancesUsed, new HashSet<>(this.tree.findAllTaxa()));
        this.distancesUsed = distancesUsed;
    }

    /**
     * Removes entries from distancesUsed where one or both taxa do not appear in the set taxa.
     * Modifies distancesUsed in place.
     * @param distancesUsed The set of distance used when constructing all cluster in a Gamma L graph
     * @param taxa The taxa set of the largest tree constructed
     */
    private Map<Pair<Identifier, Identifier>, Double> removeNonLeafTaxa(
            Map<Pair<Identifier, Identifier>, Double> distancesUsed, Set<Identifier> taxa) {
        Set<Pair<Identifier, Identifier>> remove = distancesUsed.keySet().stream()
                .filter(pair -> !(taxa.contains(pair.getLeft()) || taxa.contains(pair.getRight())))
                .collect(Collectors.toSet());
        remove.forEach(distancesUsed::remove);
        return distancesUsed;
    }

    /**
     * Write the tree and corresponding strong lasso (if update methods permits) to disk, in Nexus format.
     * @param output File to write to
     * @throws IOException
     */
    public void save(File output) throws IOException {
        //Nexus does not write treeds, but does support writing custom blocks
        NexusWriter writer = new NexusWriter();
        writer.appendHeader();
        writer.append(this.getTree().findAllTaxa());
        writer.appendLine("");
        //Write tree in newick format
        writer.appendLine("BEGIN TREES;");
        writer.appendLine("  TREE tree1 = " + this.getTree().toString() + ";");
        //Writer the strong lasso in comments
        writer.appendLine("  [Strong Lasso shown below. Cord between taxa a and b with associated weight 4 written a -> b, 4]");
        this.distancesUsed.entrySet().stream()
                .map(entry -> "  [" + entry.getKey().getLeft().getName() + " -> " + entry.getKey().getRight().getName() + ", " + entry.getValue().toString() + "]")
                .forEach(writer::appendLine);
        writer.appendLine("END; [Trees]");
        writer.write(output);
    }

    public LassoTree getTree() {
        return tree;
    }

    public Map<Pair<Identifier, Identifier>, Double> getDistancesUsed() {
        return distancesUsed;
    }
}
