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

import java.io.File;
import java.util.List;
import java.util.Map;

public class LassoResult {
    private LassoTree tree;
    private Map<Pair<Identifier, Identifier>, Double> distancesUsed;

    public LassoResult(LassoTree tree, Map<Pair<Identifier, Identifier>, Double> distancesUsed) {
        this.tree = tree;
        tree.removeInternalIdentifier();
        this.distancesUsed = distancesUsed;
    }

    public void save(File output) {
        //TODO: Code to write to file
        System.out.println("Not implemented - writing tree to screen");
        System.out.println(tree.toString());
    }

    public LassoTree getTree() {
        return tree;
    }

    public Map<Pair<Identifier, Identifier>, Double> getDistancesUsed() {
        return distancesUsed;
    }
}
