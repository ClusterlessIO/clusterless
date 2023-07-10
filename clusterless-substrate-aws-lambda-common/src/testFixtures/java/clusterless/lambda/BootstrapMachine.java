/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.lambda;

import clusterless.substrate.aws.sdk.EventBus;
import clusterless.substrate.aws.sdk.S3;
import clusterless.substrate.aws.sdk.SQS;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BootstrapMachine {
    private static final Logger LOG = LogManager.getLogger(BootstrapMachine.class);
    S3 s3 = new S3();
    EventBus eventBus = new EventBus();
    SQS sqs = new SQS();

    public BootstrapMachine() {
    }

    public BootstrapMachine applyBucket(String bucketName) {
        if (bucketName == null) {
            return this;
        }

        LOG.info("creating bucket: {}", bucketName);

        s3.create(bucketName)
                .isSuccessOrThrowRuntime();

        LOG.info("created bucket: {}", bucketName);

        return this;
    }

    public BootstrapMachine applyEventbus(String eventBusName) {
        if (eventBusName == null) {
            return this;
        }

        LOG.info("creating eventbus: {}", eventBusName);

        eventBus.create(eventBusName)
                .isSuccessOrThrowRuntime();

        LOG.info("created eventbus: {}", eventBusName);

        return this;
    }

    public BootstrapMachine applySQSQueue(String sqsQueueName) {
        if (sqsQueueName == null) {
            return this;
        }

        LOG.info("creating sqs queue: {}", sqsQueueName);

        sqs.create(sqsQueueName)
                .isSuccessOrThrowRuntime();

        LOG.info("created sqs queue: {}", sqsQueueName);

        return this;
    }
}
