/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.aws.lambda.boundary;

import clusterless.cls.json.JsonRequiredProperty;
import clusterless.cls.model.Struct;
import clusterless.cls.model.deploy.SinkDataset;
import clusterless.cls.model.deploy.partial.PathFilter;
import clusterless.cls.substrate.uri.ManifestURI;

public class BoundaryProps implements Struct {
    @JsonRequiredProperty
    protected String lotUnit;
    @JsonRequiredProperty
    protected ManifestURI manifestCompletePath;
    @JsonRequiredProperty
    protected ManifestURI manifestPartialPath;
    @JsonRequiredProperty
    protected SinkDataset dataset;
    protected String eventBusName;
    protected PathFilter filter = new PathFilter();

    public String lotUnit() {
        return lotUnit;
    }

    public ManifestURI manifestCompletePath() {
        return manifestCompletePath;
    }

    public ManifestURI manifestPartialPath() {
        return manifestPartialPath;
    }

    public SinkDataset dataset() {
        return dataset;
    }

    public String eventBusName() {
        return eventBusName;
    }

    public PathFilter filter() {
        return filter;
    }
}
