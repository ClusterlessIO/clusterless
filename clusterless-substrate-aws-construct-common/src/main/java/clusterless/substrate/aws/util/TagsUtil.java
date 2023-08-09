/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awscdk.TagProps;
import software.amazon.awscdk.Tags;
import software.constructs.IConstruct;

import java.util.Map;

/**
 *
 */
public class TagsUtil {
    private static final Logger LOG = LoggerFactory.getLogger(TagsUtil.class);
    private static boolean enabled = true;

    public static void disable() {
        LOG.info("globally disabling tags");
        enabled = false;
    }

    public static void applyTags(IConstruct target, Map<String, String> tagMap) {
        applyTags(target, tagMap, null);
    }

    public static void applyTags(IConstruct target, Map<String, String> tagMap, TagProps tagProps) {
        if (!enabled || tagMap == null || tagMap.isEmpty()) {
            return;
        }

        Tags tags = Tags.of(target);

        tagMap.forEach((key, value) -> tags.add(key, value, tagProps));
    }
}
