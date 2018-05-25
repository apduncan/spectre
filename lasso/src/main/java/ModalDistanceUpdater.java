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

import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class ModalDistanceUpdater extends DistanceUpdater {

    public ModalDistanceUpdater(LassoOptions options) {
        super(options);
    }

    @Override
    public void update(LassoDistanceGraph graph, final Identifier clusterParent) {
        final double minWeight = graph.getMinEdgeWeight();
        //Get cluster represented by parent
        LassoTree cluster = graph.getCluster(clusterParent);
        //Update distances from vertex to cluster parent
        this.clusterNeighbours(graph, cluster).entrySet().parallelStream().forEach((entry) -> {
            Identifier key = entry.getKey();
            Set<Identifier> value = entry.getValue();
            Double modal = this.modalDistance(key, value, minWeight, graph);
            //Set distance between new cluster parent and vertex not in cluster to modal distance
            graph.setDistance(clusterParent, key, modal);
        });
        //Remove cluster children from graph
        cluster.getBranches().stream().map(NewickNode::getTaxon).forEach(graph::removeTaxon);
        //TODO: distancesUsed and distanceSupport logic
    }

    private Double modalDistance(final Identifier vertex, final Set<Identifier> cluster, final double minWeight, LassoDistanceGraph graph) {
        //Find modal distance
        Map<Double, Long> counts = cluster.stream()
                .map(v -> graph.getDistance(v, vertex))
                //Ignore edges with minimum weight or 0
                .filter(d -> d > minWeight)
                .collect(Collectors.groupingBy(d -> d, Collectors.counting()));
        return counts.entrySet().stream()
                .reduce(null, (a, b) -> {
                    if(a == null)
                        return b;
                    else if(b.getValue() > a.getValue())
                        return b;
                    else if(b.getValue() == a.getValue())
                        //If equal pick one at random
                        //TODO: Could this be improved by some heuristic for picking?
                        return new Random().nextBoolean() ? a : b;
                    else
                        return a;
                }).getKey();
    }
}
