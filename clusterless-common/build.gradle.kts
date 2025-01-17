/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

plugins {
    id("clusterless.java-library-conventions")
}

dependencies {
    api("io.clusterless:clusterless-commons-core")
    api("com.google.guava:guava")
    api("one.util:streamex")
    api("com.fasterxml.jackson.core:jackson-core")
    api("com.fasterxml.jackson.core:jackson-databind")
    api("com.fasterxml.jackson.datatype:jackson-datatype-joda")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-toml")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-properties")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-csv")
}
