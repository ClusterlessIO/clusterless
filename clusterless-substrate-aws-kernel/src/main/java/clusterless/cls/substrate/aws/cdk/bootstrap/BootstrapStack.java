/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.cdk.bootstrap;

import clusterless.cls.substrate.aws.cdk.bootstrap.vpc.VPCConstruct;
import clusterless.cls.substrate.aws.resources.BootstrapStores;
import clusterless.cls.substrate.aws.resources.ClsBootstrap;
import clusterless.cls.substrate.aws.resources.Events;
import clusterless.cls.substrate.aws.util.ErrorsUtil;
import clusterless.commons.naming.Label;
import clusterless.commons.naming.Ref;
import clusterless.commons.substrate.aws.cdk.scoped.ScopedApp;
import clusterless.commons.substrate.aws.cdk.scoped.ScopedStack;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.events.EventBus;
import software.amazon.awscdk.services.s3.BlockPublicAccess;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.BucketEncryption;

import java.util.Objects;

import static clusterless.cls.substrate.aws.resources.Events.ARC_EVENT_BUS;
import static clusterless.cls.substrate.aws.resources.Events.EVENT_BUS;
import static clusterless.cls.substrate.aws.resources.Vpcs.COMMON_VPC;
import static clusterless.cls.substrate.aws.resources.Vpcs.VPC;
import static clusterless.cls.substrate.store.StateStore.*;

/**
 * keys to consider exporting
 * - BootstrapVersion
 * - BucketDomainName
 * - BucketName
 * - ImageRepositoryName
 */
public class BootstrapStack extends ScopedStack {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(BootstrapStack.class);

    public BootstrapStack(@NotNull ScopedApp app, @NotNull StackProps props) {
        super(app, "ClusterlessBootstrapStack", props);

        constructStack(app, props);
    }

    protected void constructStack(@NotNull ScopedApp app, @NotNull StackProps props) {
        Environment env = props.getEnv();

        Objects.requireNonNull(env);

        String metadataBucketName = BootstrapStores.metadataStoreName(this);
        String arcStateBucketName = BootstrapStores.arcStateStoreName(this);
        String manifestBucketName = BootstrapStores.manifestStoreName(this);

        String arcEventBusName = Events.arcEventBusName(this);

        EventBus.Builder.create(this, "ArcEventBus")
                .eventBusName(arcEventBusName)
                .build();

        VPCConstruct vpcConstruct = new VPCConstruct(this);

        Bucket metadata = constructSharedBucket(metadataBucketName, stage().with("Metadata"));
        Bucket arcState = constructSharedBucket(arcStateBucketName, stage().with("ArcState"));
        Bucket manifest = constructSharedBucket(manifestBucketName, stage().with("Manifest"));

        BootstrapMeta bootstrapMeta = (BootstrapMeta) app.stagedMeta();

        bootstrapMeta.setVersion(ClsBootstrap.BOOTSTRAP_VERSION);

        Ref metaRef = Ref.ref().withResourceType(Meta.typeKey()).withResourceName("store");
        exportNameRefFor(metaRef, metadataBucketName, "clusterless metadata bucket name");
        exportArnRefFor(metaRef, metadata.getBucketArn(), "clusterless metadata bucket arn");

        Ref arcStateRef = Ref.ref().withResourceType(Arc.typeKey()).withResourceName("store");
        exportNameRefFor(arcStateRef, arcStateBucketName, "clusterless arc state bucket name");
        exportArnRefFor(arcStateRef, arcState.getBucketArn(), "clusterless arc state bucket arn");

        Ref manifestRef = Ref.ref().withResourceType(Manifest.typeKey()).withResourceName("store");
        exportNameRefFor(manifestRef, manifestBucketName, "clusterless manifest bucket name");
        exportArnRefFor(manifestRef, manifest.getBucketArn(), "clusterless manifest bucket arn");

        Ref eventBusRef = Ref.ref().withResourceType(EVENT_BUS).withResourceName(ARC_EVENT_BUS);
        exportNameRefFor(eventBusRef, arcEventBusName, "clusterless arc event bus name");

        Ref vpcRef = Ref.ref().withResourceType(VPC).withResourceName(COMMON_VPC);
        exportIdRefFor(vpcRef, vpcConstruct.vpcId(), "clusterless vpc id");
        exportArnRefFor(vpcRef, vpcConstruct.vpcArn(), "clusterless vpc arn");
        exportNameRefFor(vpcRef, vpcConstruct.vpcName(), "clusterless vpc name");
    }

    private Bucket constructSharedBucket(String bucketName, Label prefix) {
        LOG.info("initializing {} bucket: {}", prefix.lowerHyphen(), bucketName);

        return ErrorsUtil.construct(() -> Bucket.Builder.create(this, bucketName)
                .blockPublicAccess(BlockPublicAccess.BLOCK_ALL)
                .encryption(BucketEncryption.S3_MANAGED)
                .enforceSsl(true)
                .versioned(true)
                .bucketName(bucketName)
                .removalPolicy(RemovalPolicy.DESTROY)
                .autoDeleteObjects(true)
                .build(), LOG);
    }

    @Override
    protected Ref withContext(Ref ref) {
        return super.withContext(ref)
                .withScope("bootstrap")
                .withScopeVersion(ClsBootstrap.BOOTSTRAP_VERSION)
                .withResourceNs("meta");
    }
}
