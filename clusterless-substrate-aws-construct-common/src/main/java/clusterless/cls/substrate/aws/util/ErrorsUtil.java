/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.util;

import org.slf4j.Logger;
import software.constructs.IConstruct;

import java.util.function.Supplier;

/**
 *
 */
public class ErrorsUtil {
    public static <R extends IConstruct> R construct(Supplier<R> supplier, Logger log) {
        return construct(null, supplier, log);
    }

    public static <R extends IConstruct> R construct(String expectedType, Supplier<R> supplier, Logger log) {
        try {
            return supplier.get();
        } catch (software.amazon.jsii.JsiiException error) {
            String errorMessage = error.getMessage();

            if (expectedType == null || expectedType.isEmpty()) {
                log.error("failed constructing object with: {}", errorMessage);
            } else {
                log.error("failed constructing: {}, with: {}", expectedType, errorMessage);
            }

            throw new IllegalStateException(errorMessage, error);
        }
    }
}
