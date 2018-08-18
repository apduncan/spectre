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

package uk.ac.uea.cmp.spectre.lasso.rooted;
import uk.ac.uea.cmp.spectre.core.ds.Identifier;
import uk.ac.uea.cmp.spectre.core.ds.tree.newick.NewickNode;
import uk.ac.uea.cmp.spectre.lasso.LassoDistanceGraph;
import uk.ac.uea.cmp.spectre.lasso.LassoTree;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class DistanceUpdater {
    private RootedLassoOptions options;

    public DistanceUpdater(RootedLassoOptions options) {
        this.options = options;
    }

    public abstract Map<Identifier, Set<Identifier>> update(LassoDistanceGraph graph, Identifier clusterParent);

    /**
     * Find vertices which neighbour any member of the cluster, and make a set of the vertices in the cluster
     * which it neighbours
     * @param graph The graph
     * @param cluster The cluster being formed
     * @return Map where the key is a vertex which is adjacent to to the cluster, the value is a set of vertices in the
     * cluster which the vertex is adjacent to.
     */
    public Map<Identifier, Set<Identifier>> clusterNeighbours(LassoDistanceGraph graph, LassoTree cluster) {
        //Set of identifiers in cluster
        final Set<Identifier> clusterVertices = cluster.getBranches().stream()
                .map(NewickNode::getTaxon)
                .collect(Collectors.toSet());
        return clusterVertices.stream()
                .map(graph::getNeighbours)
                .flatMap(Set::stream)
                .filter(v -> !clusterVertices.contains(v))
                //eliminate any duplicates
                .distinct()
                .collect(Collectors.toMap(
                        vertex -> vertex,
                        vertex -> {
                            //Get only those neighbours who are in the cluster
                            Set<Identifier> inCluster = graph.getNeighbours(Identifier.class.cast(vertex));
                            inCluster.retainAll(clusterVertices);
                            return inCluster;
                        }
                ));
    }
}
