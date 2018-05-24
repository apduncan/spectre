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

import java.util.Set;

public abstract class CliqueFinder {
    private LassoOptions options;
    public CliqueFinder(LassoOptions options) {
        this.options = options;
    }

    public abstract Set<Identifier> find(LassoDistanceGraph graph);

    public LassoOptions getOptions() {
        return options;
    }

    public void setOptions(LassoOptions options) {
        this.options = options;
    }
}
