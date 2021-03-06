/*
 * Suite of PhylogEnetiC Tools for Reticulate Evolution (SPECTRE)
 * Copyright (C) 2017  UEA School of Computing Sciences
 *
 * This program is free software: you can redistribute it and/or modify it under the term of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package uk.ac.uea.cmp.spectre.core.ds.quad.quartet.load;

import org.kohsuke.MetaInfServices;
import uk.ac.uea.cmp.spectre.core.ds.distance.DistanceMatrix;
import uk.ac.uea.cmp.spectre.core.ds.quad.quartet.CanonicalWeightedQuartetMap;
import uk.ac.uea.cmp.spectre.core.ds.quad.quartet.QuartetSystem;
import uk.ac.uea.cmp.spectre.core.io.nexus.NexusReader;

import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA. User: Analysis Date: 2004-jul-11 Time: 23:09:07 To
 * change this template use Options | File Templates.
 */
@MetaInfServices(QLoader.class)
public class NexusDistancesLoader extends AbstractNexusLoader {

    @Override
    public QuartetSystem load(File file) throws IOException {

        // Load distance matrix from file
        DistanceMatrix distanceMatrix = new NexusReader().readDistanceMatrix(file);

        // Create QuartetWeightings from distance matrix
        CanonicalWeightedQuartetMap qw = new CanonicalWeightedQuartetMap(distanceMatrix);

        // Create and return the quartet network
        return new QuartetSystem(distanceMatrix.getTaxa(), 1.0, qw);
    }

    @Override
    public String getName() {
        return "nexus:distances";
    }
}
