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

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import uk.ac.uea.cmp.spectre.core.ds.Identifier;
import uk.ac.uea.cmp.spectre.core.ds.tree.newick.NewickNode;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ModalDistanceUpdater extends DistanceUpdater {

    public ModalDistanceUpdater(LassoOptions options) {
        super(options);
    }

    @Override
    public Map<Identifier, Set<Identifier>> update(LassoDistanceGraph graph, final Identifier clusterParent) {
        final double minWeight = graph.getMinEdgeWeight();
        //Get cluster represented by parent
        LassoTree cluster = graph.getCluster(clusterParent);
        //Update distances from vertex to cluster parent
        //Distances justifying retained edges should be returned in map
        //Only relevant if returning a strong Lasso, such as in this approach
        Map<Identifier, Set<Identifier>> support = new ConcurrentHashMap<>();
        this.clusterNeighbours(graph, cluster).entrySet().parallelStream().forEach((entry) -> {
            Identifier key = entry.getKey();
            Set<Identifier> value = entry.getValue();
            Pair<Double, Set<Identifier>> modal = this.modalDistance(key, value, minWeight, graph);
            //Set distance between new cluster parent and vertex not in cluster to modal distance
            if(modal != null) {
                graph.setDistance(clusterParent, key, modal.getLeft());
                support.put(key, modal.getRight());
            }
        });
        //Remove cluster children from graph
        cluster.getBranches().stream().map(NewickNode::getTaxon).forEach(graph::removeTaxon);
        return support;
    }

    private Pair<Double, Set<Identifier>> modalDistance(final Identifier vertex, final Set<Identifier> cluster, final double minWeight, LassoDistanceGraph graph) {
        //Find modal distance
        Map<Double, List<Identifier>> counts = cluster.stream()
                //Ignore edges with minimum weight or 0
                .filter(v -> graph.getDistance(v, vertex) > minWeight)
                .collect(Collectors.groupingBy(v -> graph.getDistance(v, vertex)));
        Map.Entry<Double, List<Identifier>> modal = counts.entrySet().stream()
                .reduce(null, (a, b) -> {
                    if(a == null)
                        return b;
                    else if(b.getValue().size() > a.getValue().size())
                        return b;
                    else if(b.getValue().size() == a.getValue().size())
                        //If equal pick one at random
                        //TODO: Could this be improved by some heuristic for picking?
                        return new Random().nextBoolean() ? a : b;
                    else
                        return a;
                });
        return modal == null ? null : new ImmutablePair<>(modal.getKey(), new HashSet<>(modal.getValue()));
    }
}
