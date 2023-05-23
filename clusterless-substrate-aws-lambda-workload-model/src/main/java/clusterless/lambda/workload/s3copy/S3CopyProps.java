/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.lambda.workload.s3copy;

import clusterless.model.deploy.WorkloadProps;

public class S3CopyProps extends WorkloadProps {
    float failArcOnPartialPercent = 0f;

    public float failArcOnPartialPercent() {
        return failArcOnPartialPercent;
    }
}