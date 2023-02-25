/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    java
    idea
}

version = "1.0-wip"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(19))
        vendor.set(JvmVendorSpec.GRAAL_VM)
    }
}

dependencies {
    implementation("org.apache.logging.log4j:log4j-api")
    implementation("org.apache.logging.log4j:log4j-core")
    implementation("org.apache.logging.log4j:log4j-jul")

    compileOnly("javax.annotation:javax.annotation-api")

    constraints {
        // manage dependency versions here
        implementation("info.picocli:picocli:4.7.1")

        implementation("org.apache.logging.log4j:log4j-api:2.19.0")
        implementation("org.apache.logging.log4j:log4j-core:2.19.0")
        implementation("org.apache.logging.log4j:log4j-jul:2.19.0")
        implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.19.0")

        implementation("com.google.guava:guava:31.1-jre")

        implementation("com.fasterxml.jackson.core:jackson-core:2.14.1")
        implementation("com.fasterxml.jackson.core:jackson-databind:2.14.1")
        implementation("com.fasterxml.jackson.datatype:jackson-datatype-joda:2.14.1")
        implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.14.1")

        implementation("software.amazon.awssdk:s3:2.19.29")
        implementation("software.amazon.awssdk:cloudwatch:2.19.29")
        implementation("software.amazon.awssdk:eventbridge:2.19.29")

        // https://github.com/aws/aws-lambda-java-libs
        implementation("com.amazonaws:aws-lambda-java-core:1.2.2")
        implementation("com.amazonaws:aws-lambda-java-events:3.11.0")
        implementation("com.amazonaws:aws-lambda-java-serialization:1.0.1")
        implementation("com.amazonaws:aws-lambda-java-log4j2:1.5.1")

        implementation("javax.annotation:javax.annotation-api:1.3.2")

        implementation("io.github.resilience4j:resilience4j-retry:2.0.2")

        testImplementation("com.amazonaws:aws-lambda-java-tests:1.1.1")

        // https://github.com/junit-pioneer/junit-pioneer/releases
        testImplementation("org.junit-pioneer:junit-pioneer:2.0.0")
        testImplementation("org.junit-pioneer:junit-pioneer-jackson:2.0.0")

        // https://github.com/hosuaby/inject-resources
        testImplementation("io.hosuaby:inject-resources-core:0.3.2")
        testImplementation("io.hosuaby:inject-resources-junit-jupiter:0.3.2")

        // https://github.com/webcompere/system-stubs
        testImplementation("uk.org.webcompere:system-stubs-core:2.0.2")
        testImplementation("uk.org.webcompere:system-stubs-jupiter:2.0.2")
        testImplementation("org.mockito:mockito-inline:5.1.1")

        testImplementation("org.testcontainers:testcontainers:1.17.6")
        testImplementation("org.testcontainers:junit-jupiter:1.17.6")
        testImplementation("org.testcontainers:localstack:1.17.6")
        // https://github.com/testcontainers/testcontainers-java/issues/1442#issuecomment-694342883
        testImplementation("com.amazonaws:aws-java-sdk-s3:1.11.860")

        testImplementation("org.apache.logging.log4j:log4j-slf4j-impl:2.19.0")
    }
}

tasks.test {
    useJUnitPlatform()
    filter {
        isFailOnNoMatchingTests = false
        excludeTestsMatching("*HandlerTest")
    }
}

tasks.register<Test>("integrationTest") {
    useJUnitPlatform()
    filter {
        isFailOnNoMatchingTests = false
        includeTestsMatching("*HandlerTest")
    }
}

tasks.named("check").get()
    .dependsOn("integrationTest")


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
                implementation("org.testcontainers:testcontainers")
                implementation("org.testcontainers:junit-jupiter")
                implementation("org.testcontainers:localstack")
                implementation("com.amazonaws:aws-java-sdk-s3")

                implementation("org.apache.logging.log4j:log4j-slf4j-impl") // used by inject-resources
            }
        }
    }
}
