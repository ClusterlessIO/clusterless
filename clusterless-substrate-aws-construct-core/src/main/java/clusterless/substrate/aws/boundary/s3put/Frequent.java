/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.boundary.s3put;

import clusterless.model.Struct;

public class Frequent implements Struct {
    int queueFetchWaitSeconds = 0;

    public int queueFetchWaitSeconds() {
        return queueFetchWaitSeconds;
    }
}
