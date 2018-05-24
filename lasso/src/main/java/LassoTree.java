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

import uk.ac.uea.cmp.spectre.core.ds.Identifier;
import uk.ac.uea.cmp.spectre.core.ds.tree.newick.NewickNode;

import java.util.List;

public class LassoTree extends NewickNode {
    public LassoTree() {
        super();
    }

    public LassoTree(LassoTree copy) {
        this.length = copy.length;
        if(copy.taxon != null)
            this.taxon = new Identifier(copy.taxon);
        //Recursively copy tree
        copy.branches.parallelStream().forEach((node) -> this.branches.add(new LassoTree((LassoTree)node)));
    }

    public LassoTree(Identifier vertex) {
        this();
        this.taxon = vertex;
    }

    public double getRootHeight() {
        if(this.branches.size() > 0)
            return this.branches.get(0).getLength() + ((LassoTree)this.branches.get(0)).getRootHeight();
        else
            return 0;
    }

    public void removeInternalIdentifier() {
        if(!this.isLeaf()) {
            this.setTaxon(null);
            this.getBranches().stream().map(LassoTree.class::cast).forEach(LassoTree::removeInternalIdentifier);
        }
    }
}