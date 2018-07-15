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

import uk.ac.uea.cmp.spectre.core.ds.Identifier;
import uk.ac.uea.cmp.spectre.lasso.LassoDistanceGraph;

import java.util.*;

public class BKCliqueFinder extends CliqueFinder {
    private List<Set<Identifier>> cliques;
    private LassoDistanceGraph graph;
    private Map<Identifier, Set<Identifier>> neighbours;
    private Double minWeight;

    public BKCliqueFinder(LassoOptions options) {
        super(options);
    }

    @Override
    public Set<Identifier> find(LassoDistanceGraph graph) {
        this.graph = graph;
        this.neighbours = new HashMap<>();
        this.minWeight = this.graph.getMinEdgeWeight();
        this.cliques = new ArrayList<>();
        //initialise r and x to empty set, p to vertex set
        Set<Identifier> r = new HashSet<>();
        Set<Identifier> x = new HashSet<>();
        Set<Identifier> p = new HashSet<>(this.graph.getTaxa());
        this.BKTomita(r, p, x);
        return this.cliques.stream().max(Comparator.comparingInt(Set::size)).get();
    }

    private void BKTomita(Set<Identifier> r, Set<Identifier> p, Set<Identifier> x) {
        if(p.size() == 0 && x.size() == 0) {
            this.cliques.add(r);
        } else {
            //Select pivot vertex from p union x
            Set<Identifier> pivotCandidates = new HashSet(p);
            pivotCandidates.addAll(x);
            //Use Tomita criteria to select pivot - vertex with highest number neighbours in p
            Identifier pivot = pivotCandidates.stream()
                    .max(Comparator.comparingInt(vertex -> {
                        Set<Identifier> copyP = new HashSet<>(p);
                        copyP.retainAll(this.getNeighbours(vertex));
                        return copyP.size();
                    })).get();
            //Update candidate set
            Set<Identifier> candidates = new HashSet<>(p);
            candidates.removeAll(this.getNeighbours(pivot));
            //Set<Identifier> iterP =  new HashSet<>(p);
            for(Identifier v : candidates) {
                Set<Identifier> newR = new HashSet<>(r);
                newR.add(v);
                Set<Identifier> newP = new HashSet<>(p);
                newP.retainAll(this.getNeighbours(v));
                Set<Identifier> newX = new HashSet<>(x);
                newX.retainAll(this.getNeighbours(v));
                this.BKTomita(newR, newP, newX);
                //update sets for next branch - move v from p to x
                p.remove(v);
                x.add(v);
            }
        }
    }

    private Set<Identifier> getNeighbours(Identifier vertex) {
        if(!this.neighbours.containsKey(vertex)) {
            this.neighbours.put(vertex, graph.getNeighbours(vertex, this.minWeight));
        }
        return new HashSet<>(this.neighbours.get(vertex));
    }
}
