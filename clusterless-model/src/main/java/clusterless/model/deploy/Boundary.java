/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.model.deploy;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 */
public abstract class Boundary extends Extensible {
    @JsonProperty(required = true)
    String name;

    @JsonProperty(required = true)
    SinkDataset dataset = new SinkDataset();

    public String name() {
        return name;
    }

    public SinkDataset dataset() {
        return dataset;
    }
}