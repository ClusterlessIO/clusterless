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
    implementation(project(":clusterless-substrate-aws-common"))

    api(project(":clusterless-substrate-aws-lambda-common-model"))

    implementation("com.google.guava:guava")

    implementation("com.fasterxml.jackson.core:jackson-core")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-joda")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    api("software.amazon.awssdk:s3")
    api("software.amazon.awssdk:cloudwatch")
    api("software.amazon.awssdk:eventbridge")
    api("software.amazon.awssdk:glue")

    // https://docs.aws.amazon.com/lambda/latest/dg/java-logging.html#java-logging-log4j2
    api("com.amazonaws:aws-lambda-java-core")
    api("com.amazonaws:aws-lambda-java-log4j2")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl")
    implementation("org.apache.logging.log4j:log4j-layout-template-json")

    testFixturesImplementation(project(":clusterless-common"))
    testFixturesImplementation(project(":clusterless-model"))
    testFixturesImplementation(project(":clusterless-substrate-aws-common"))

    testFixturesImplementation("io.clusterless:clusterless-commons-aws")
    testFixturesImplementation("com.google.guava:guava")

    testFixturesImplementation("com.fasterxml.jackson.core:jackson-core")
    testFixturesImplementation("com.fasterxml.jackson.core:jackson-databind")
    testFixturesImplementation("com.fasterxml.jackson.datatype:jackson-datatype-joda")
    testFixturesImplementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    testFixturesImplementation("org.mockito:mockito-core")

    testFixturesImplementation("org.testcontainers:testcontainers")
    testFixturesImplementation("org.testcontainers:junit-jupiter")
    testFixturesImplementation("org.testcontainers:localstack")
    testFixturesImplementation("com.amazonaws:aws-java-sdk-s3")
    testFixturesImplementation("io.hosuaby:inject-resources-core")
    testFixturesImplementation("io.hosuaby:inject-resources-junit-jupiter")
    testFixturesImplementation("uk.org.webcompere:system-stubs-core")
    testFixturesImplementation("uk.org.webcompere:system-stubs-jupiter")
    testFixturesImplementation("org.mockito:mockito-inline")
}
