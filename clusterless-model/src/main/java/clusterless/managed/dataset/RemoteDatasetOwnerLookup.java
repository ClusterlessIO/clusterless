/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.managed.dataset;

import clusterless.model.deploy.OwnedDataset;
import clusterless.model.deploy.Placement;
import clusterless.model.deploy.SourceDataset;

import java.util.Optional;

@FunctionalInterface
public interface RemoteDatasetOwnerLookup {
    Optional<OwnedDataset> lookup(Placement placement, SourceDataset source);
}
