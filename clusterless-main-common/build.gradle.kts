/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

plugins {
    id("clusterless.java-library-conventions")
}

dependencies {
    implementation(project(":clusterless-model"))

    implementation("info.picocli:picocli")

//
//    implementation("com.google.guava:guava")
//    implementation("com.fasterxml.jackson.core:jackson-core")
//    implementation("com.fasterxml.jackson.core:jackson-databind")
//    implementation("com.fasterxml.jackson.datatype:jackson-datatype-joda")
//    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
}