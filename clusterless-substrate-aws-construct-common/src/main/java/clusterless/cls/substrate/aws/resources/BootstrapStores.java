/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.resources;

import clusterless.cls.substrate.store.StateStore;
import clusterless.cls.substrate.store.Stores;
import clusterless.commons.naming.Label;
import clusterless.commons.naming.Ref;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.Fn;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.IBucket;
import software.constructs.Construct;

/**
 *
 */
public class BootstrapStores {

    public static String metadataStoreName(@NotNull Construct scope) {
        return bootstrapStoreName(scope, StateStore.Meta);
    }

    public static @NotNull IBucket metadataBucket(@NotNull Construct scope) {
        return fromBucket(scope, StateStore.Meta);
    }

    public static String arcStateStoreName(@NotNull Construct scope) {
        return bootstrapStoreName(scope, StateStore.Arc);
    }

    public static @NotNull IBucket arcStateBucket(Construct scope) {
        return fromBucket(scope, StateStore.Arc);
    }

    public static String manifestStoreName(@NotNull Construct scope) {
        return bootstrapStoreName(scope, StateStore.Manifest);
    }

    public static @NotNull IBucket manifestBucket(@NotNull Construct scope) {
        return fromBucket(scope, StateStore.Manifest);
    }

    public static String manifestStoreNameRef(@NotNull Construct scope) {
        return importName(scope, StateStore.Manifest);
    }

    private static String importName(@NotNull Construct scope, StateStore store) {
        Ref ref = ClsBootstrap.bootstrapBase(scope, Ref.nameRef())
                .withResourceType(store.typeKey())
                .withResourceName("store");

        return Fn.importValue(ref.exportName());
    }

    @NotNull
    private static IBucket fromBucket(Construct scope, StateStore store) {
        return Bucket.fromBucketName(scope, Label.of(store).with("Bucket").camelCase(), bootstrapStoreName(scope, store));
    }

    private static String bootstrapStoreName(@NotNull Construct scope, StateStore bucketName) {
        return Stores.bootstrapStoreName(bucketName, StateURIs.placementFor(scope));
    }
}
