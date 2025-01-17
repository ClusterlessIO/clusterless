/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

import java.io.FileInputStream
import java.util.*

plugins {
    java
    idea
    `java-test-fixtures`
}

val versionProperties = Properties().apply {
    load(FileInputStream(File(rootProject.rootDir, "version.properties")))
}

val release = false; // test if release branch
val buildNumber = System.getenv("GITHUB_RUN_NUMBER") ?: "dev"
val wipReleases = "wip-${buildNumber}"

val versionLabel = if (release)
    "${versionProperties["clusterless.release.major"]}-${versionProperties["clusterless.release.minor"]}"
else
    "${versionProperties["clusterless.release.major"]}-wip"

project.extra.set("versionLabel", versionLabel)

val versionBranch = if (release)
    "${versionProperties["clusterless.release.major"]}"
else
    "wip-${versionProperties["clusterless.release.major"]}"

project.extra.set("versionBranch", versionBranch)

version = if (release)
    "${versionProperties["clusterless.release.major"]}-${versionProperties["clusterless.release.minor"]}"
else
    "${versionProperties["clusterless.release.major"]}-${wipReleases}"

repositories {
    mavenCentral()

//    mavenLocal()
//    maven {
//        url = uri("https://maven.pkg.github.com/heretical/*")
//
//        credentials {
//            username = project.findProperty("githubUsername") as String?
//            password = project.findProperty("githubPassword") as String?
//        }
//    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
//        vendor.set(JvmVendorSpec.GRAAL_VM)
    }
}

// some explanation on how to handle dependencies, slowly evolving
// the 'constraints' force a version into the scope (implementation, testImplementation, etc.)
// this allows us to only have to declare a dependency, without the version information
// the test suites are a convenience for declaring different types of tests, e.g. unit test, and integration tests.
// within the suite the dependency is declared as a `implementation` but it's a testImplementation in practice
// this is where it gets difficult/tedious/redundant.. testFixtures - https://docs.gradle.org/8.0.1/userguide/java_testing.html#sec:java_test_fixtures
// testFixtures (declared as a plugin above) let us create re-usable libraries, that depend on implementation
// code and tests can depend on the fixture. this allows a fixture to be published (into maven) more safely than
// we could prior (by packaging tests into their own jar, when we just wanted the tools in them).
// testFixtures aren't tests and have their own dependencies, so we have to do double entry on the constraints
// testFixtures actually can't see the implementation, so a dependency needs to be declared unless it was published as
// `api` and not `implementation`
// having said all that, this might be an improved approach: https://docs.gradle.org/current/userguide/platforms.html

// helpers for preventing redundant lines
fun DependencyConstraintHandlerScope.implementationAndTestFixture(constraintNotation: String) {
    implementation(constraintNotation)
    testFixturesImplementation(constraintNotation)
}

fun DependencyConstraintHandlerScope.testImplementationAndTestFixture(constraintNotation: String) {
    testImplementation(constraintNotation)
    testFixturesImplementation(constraintNotation)
}

dependencies {
    implementation("org.slf4j:slf4j-api")

    implementation("org.jetbrains:annotations")

    compileOnly("javax.annotation:javax.annotation-api")

    constraints {
        // manage dependency versions here

        val commons = "0.10"
        implementationAndTestFixture("io.clusterless:clusterless-commons-core:$commons")
        implementationAndTestFixture("io.clusterless:clusterless-commons-aws:$commons")

        implementation("io.heretical:mini-parsers-temporal:2.0.0")

        implementation("org.jetbrains:annotations:24.0.0")
        implementation("info.picocli:picocli:4.7.6")
        implementation("com.github.jknack:handlebars:4.4.0")
//        implementation("com.cronutils:cron-utils:9.2.0")

        val slf4j = "2.0.16"
        implementation("org.slf4j:slf4j-api:$slf4j")

        val logback = "1.5.16"
        implementation("ch.qos.logback:logback-classic:$logback")
        implementation("ch.qos.logback:logback-core:$logback")

        // only used by lambdas
        // https://mvnrepository.com/artifact/org.apache.logging.log4j
        val log4j = "2.24.3"
        implementation("org.apache.logging.log4j:log4j-api:$log4j")
        implementation("org.apache.logging.log4j:log4j-core:$log4j")
        implementation("org.apache.logging.log4j:log4j-jul:$log4j")
        implementation("org.apache.logging.log4j:log4j-slf4j2-impl:$log4j")
        implementation("org.apache.logging.log4j:log4j-to-slf4j:$log4j")
        implementation("org.apache.logging.log4j:log4j-web:$log4j")
        implementation("org.apache.logging.log4j:log4j-layout-template-json:$log4j")

        implementationAndTestFixture("com.jayway.jsonpath:json-path:2.9.0")
        implementationAndTestFixture("com.google.guava:guava:33.4.0-jre")

        // https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-core
        val jackson = "2.18.2"
        implementationAndTestFixture("com.fasterxml.jackson.core:jackson-core:$jackson")
        implementationAndTestFixture("com.fasterxml.jackson.core:jackson-databind:$jackson")
        implementationAndTestFixture("com.fasterxml.jackson.datatype:jackson-datatype-joda:$jackson")
        implementationAndTestFixture("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jackson")
        implementationAndTestFixture("com.fasterxml.jackson.dataformat:jackson-dataformat-toml:$jackson")
        implementationAndTestFixture("com.fasterxml.jackson.dataformat:jackson-dataformat-properties:$jackson")
        implementationAndTestFixture("com.fasterxml.jackson.dataformat:jackson-dataformat-csv:$jackson")

        implementationAndTestFixture("one.util:streamex:0.8.2")

        // https://mvnrepository.com/artifact/software.amazon.awssdk
        val awsSdk = "2.29.51"
        implementationAndTestFixture("software.amazon.awssdk:s3:$awsSdk")
        implementationAndTestFixture("software.amazon.awssdk:cloudwatch:$awsSdk")
        implementationAndTestFixture("software.amazon.awssdk:cloudwatchlogs:$awsSdk")
        implementationAndTestFixture("software.amazon.awssdk:eventbridge:$awsSdk")
        implementationAndTestFixture("software.amazon.awssdk:sqs:$awsSdk")
        implementationAndTestFixture("software.amazon.awssdk:glue:$awsSdk")
        implementationAndTestFixture("software.amazon.awssdk:athena:$awsSdk")

        // https://github.com/aws/aws-lambda-java-libs
        implementationAndTestFixture("com.amazonaws:aws-lambda-java-core:1.2.3")
        implementationAndTestFixture("com.amazonaws:aws-lambda-java-events:3.11.3")
        implementationAndTestFixture("com.amazonaws:aws-lambda-java-serialization:1.1.4")
        implementationAndTestFixture("com.amazonaws:aws-lambda-java-log4j2:1.6.0")
        implementationAndTestFixture("com.amazonaws:aws-lambda-java-tests:1.1.1")

        implementationAndTestFixture("javax.annotation:javax.annotation-api:1.3.2")

        implementationAndTestFixture("io.github.resilience4j:resilience4j-retry:2.1.0")

        testImplementationAndTestFixture("org.mockito:mockito-core:5.7.0")

        // https://github.com/junit-pioneer/junit-pioneer/releases
        val junitPioneer = "2.1.0"
        testImplementationAndTestFixture("org.junit-pioneer:junit-pioneer:$junitPioneer")
        testImplementationAndTestFixture("org.junit-pioneer:junit-pioneer-jackson:$junitPioneer")

        // https://github.com/hosuaby/inject-resources
        val injectResources = "0.3.3"
        testImplementationAndTestFixture("io.hosuaby:inject-resources-core:$injectResources")
        testImplementationAndTestFixture("io.hosuaby:inject-resources-junit-jupiter:$injectResources")

        // https://github.com/webcompere/system-stubs
        val systemStubs = "2.1.3"
        testImplementationAndTestFixture("uk.org.webcompere:system-stubs-core:$systemStubs")
        testImplementationAndTestFixture("uk.org.webcompere:system-stubs-jupiter:$systemStubs")
        testImplementationAndTestFixture("org.mockito:mockito-inline:5.1.1")

        // https://mvnrepository.com/artifact/org.testcontainers
        val testContainers = "1.20.4"
        testImplementationAndTestFixture("org.testcontainers:testcontainers:$testContainers")
        testImplementationAndTestFixture("org.testcontainers:junit-jupiter:$testContainers")
        testImplementationAndTestFixture("org.testcontainers:localstack:$testContainers")
        // https://github.com/testcontainers/testcontainers-java/issues/1442#issuecomment-694342883
        testImplementationAndTestFixture("com.amazonaws:aws-java-sdk-s3:1.11.860")

        testImplementationAndTestFixture("ch.qos.logback:logback-classic:$logback")
        testImplementationAndTestFixture("ch.qos.logback:logback-core:$logback")
    }
}

testing {
    suites {
        // Configure the built-in test suite
        val test by getting(JvmTestSuite::class) {
            // Use JUnit Jupiter test framework
            useJUnitJupiter("5.9.1")
            dependencies {
                implementation("org.junit-pioneer:junit-pioneer") {
                    capabilities {
                        requireCapability("org.junit-pioneer:junit-pioneer-jackson")
                    }
                }
                implementation("org.junit.platform:junit-platform-launcher")
                implementation("io.hosuaby:inject-resources-core")
                implementation("io.hosuaby:inject-resources-junit-jupiter")
                implementation("uk.org.webcompere:system-stubs-core")
                implementation("uk.org.webcompere:system-stubs-jupiter")
                implementation("org.mockito:mockito-inline")
            }
        }

        val integrationTest by registering(JvmTestSuite::class) {
            dependencies {
                implementation(project())
                implementation("org.testcontainers:testcontainers")
                implementation("org.testcontainers:junit-jupiter")
                implementation("org.testcontainers:localstack")
                implementation("com.amazonaws:aws-java-sdk-s3")
            }

            targets {
                all {
                    testTask.configure {
                        shouldRunAfter(test)
                    }
                }
            }
        }
    }
}

val integrationTestImplementation by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}

tasks.named("check") {
    dependsOn(testing.suites.named("integrationTest"))
}
