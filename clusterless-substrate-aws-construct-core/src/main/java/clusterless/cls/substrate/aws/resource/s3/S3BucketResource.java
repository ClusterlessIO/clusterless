/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.resource.s3;

import clusterless.cls.config.ResourceConfig;
import clusterless.cls.json.JsonRequiredProperty;
import clusterless.cls.model.deploy.Resource;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Creates and maintains an S3 bucket and any associated metadata.
 */
public class S3BucketResource extends Resource {
    @JsonRequiredProperty
    private String bucketName;
    private boolean versioned = false;

    private boolean enableEventBridge = true;

    /**
     * When true, the bucket and it's data will be removed when the project is destroyed.
     * <p>
     * Unless the {@link ResourceConfig#removeAllOnDestroy()} is true.
     */
    private boolean removeOnDestroy = false;
    private Map<String, String> tags = new LinkedHashMap<>();

    public S3BucketResource() {
    }

    public String bucketName() {
        return bucketName;
    }

    public boolean versioned() {
        return versioned;
    }

    public boolean enableEventBridge() {
        return enableEventBridge;
    }

    public boolean removeOnDestroy() {
        return removeOnDestroy;
    }

    public Map<String, String> tags() {
        return tags;
    }
}
