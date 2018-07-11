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

import org.apache.commons.math3.util.Combinations;
import uk.ac.uea.cmp.spectre.core.ds.Identifier;
import uk.ac.uea.cmp.spectre.core.ds.IdentifierList;
import uk.ac.uea.cmp.spectre.core.ds.distance.DistanceMatrix;
import uk.ac.uea.cmp.spectre.core.ds.distance.RandomDistanceGenerator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Generate all possible combinations of a set of taxa chosing r elements, without duplication.
 * Uses Apache Combinations class to generate combinations of integer indices, and maps these to Identifiers.
 * Combinations generates next combination in lexical order on request, without storing all combinations in memory.
 */
public class IdentifierCombinations implements Iterable<IdentifierList> {
    private IdentifierList taxa;
    private int r;

    /**
     * Constructor
     * @param taxa List of the taxa to combination
     * @param r Select combinations of r elements from the taxa
     */
    public IdentifierCombinations(IdentifierList taxa, int r) {
        this.taxa = taxa;
        this.r = r;
    }

    @Override
    public Iterator<IdentifierList> iterator() {
        return new IdentifierIterator();
    }

    /**
     * The iterator which generate a sequence of combinations
     */
    private class IdentifierIterator implements Iterator<IdentifierList> {
        //This the underling iterator which generates int combinations
        private Iterator<int[]> combinationIterator;

        public IdentifierIterator() {
            //Each instance has its own Combinations object, so this can be iterated over when nested
            this.combinationIterator = new Combinations(taxa.size(), r).iterator();
        }

        @Override
        public boolean hasNext() {
            return this.combinationIterator.hasNext();
        }

        @Override
        public IdentifierList next() {
            return mapToIdentifiers(this.combinationIterator.next());
        }

        /**
         * Convert an array of ints to a list of Identifiers, where the identifiers are those at the indices matching
         * the ints from the array.
         * @param combination int array specifying indices from taxa which should be in this combination
         * @return
         */
        private IdentifierList mapToIdentifiers(int[] combination) {
           IdentifierList identifierCombination = new IdentifierList();
           for(int i : combination) {
               identifierCombination.add(taxa.get(i));
           }
           return identifierCombination;
        }
    }
}
