/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

plugins {
    id("clusterless.java-lambda-conventions")
}

dependencies {
    implementation(project(":clusterless-common"))
    implementation(project(":clusterless-model"))
    implementation("com.google.guava:guava")

    api(project(":clusterless-substrate"))

    implementation("com.fasterxml.jackson.core:jackson-core")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-joda")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    api("io.github.resilience4j:resilience4j-retry")
    api("com.jayway.jsonpath:json-path")

    implementation("software.amazon.awssdk:s3")
    implementation("software.amazon.awssdk:eventbridge")
    implementation("software.amazon.awssdk:sqs")
    implementation("software.amazon.awssdk:glue")
    implementation("software.amazon.awssdk:athena")
    implementation("software.amazon.awssdk:cloudwatchlogs")
}
