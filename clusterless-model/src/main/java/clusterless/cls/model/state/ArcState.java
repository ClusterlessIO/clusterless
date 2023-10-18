/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.model.state;

import clusterless.cls.model.State;
import clusterless.cls.naming.Partition;
import clusterless.cls.util.Strings;

import java.util.Locale;

/**
 * <pre>
 *  running-->complete;
 *  running-->partial;
 *  partial-->running;
 *  partial-->missing;
 *  missing-->running;
 * </pre>
 */
public enum ArcState implements State, Partition.EnumPartition {
    running,
    complete,
    partial,
    missing;

    @Override
    public String key() {
        return "state";
    }

    public static ArcState parse(String state) {
        if (Strings.emptyToNull(state) == null) {
            return null;
        }

        state = state.toLowerCase(Locale.ROOT);

        for (ArcState value : values()) {
            if (state.matches(String.format("^(.*[=])?%s([.].*)?$", value))) {
                return value;
            }
        }

        return null;
    }

}