/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.arc.s3copy;

import clusterless.aws.lambda.workload.s3copy.S3CopyProps;
import clusterless.cls.model.deploy.Arc;
import clusterless.cls.model.deploy.Workload;
import clusterless.cls.substrate.aws.props.LambdaJavaRuntimeProps;
import clusterless.cls.substrate.aws.props.Memory;

/**
 *
 */
public class S3CopyArc extends Arc<S3CopyArc.CopyWorkload> {

    public static class CopyWorkload extends Workload<S3CopyProps> {
        LambdaJavaRuntimeProps runtimeProps = new LambdaJavaRuntimeProps(
                Memory.MEM_512MB,
                3,
                15
        );

        public CopyWorkload() {
            super(new S3CopyProps());
        }

        public LambdaJavaRuntimeProps runtimeProps() {
            return runtimeProps;
        }
    }

    public S3CopyArc() {
        super(new CopyWorkload());
    }
}
