/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

import gradle.kotlin.dsl.accessors._4997f459ad541c4ce4f563c80c21be18.implementation

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

    testImplementation("org.junit-pioneer:junit-pioneer")

    constraints {
        // manage dependency versions here

        implementation("info.picocli:picocli:4.7.1")

        implementation("org.apache.logging.log4j:log4j-api:2.19.0")
        implementation("org.apache.logging.log4j:log4j-core:2.19.0")
        implementation("org.apache.logging.log4j:log4j-jul:2.19.0")

        implementation("com.google.guava:guava:31.1-jre")

        implementation("com.fasterxml.jackson.core:jackson-core:2.14.1")
        implementation("com.fasterxml.jackson.core:jackson-databind:2.14.1")
        implementation("com.fasterxml.jackson.datatype:jackson-datatype-joda:2.14.1")
        implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.14.1")

        testImplementation("org.junit-pioneer:junit-pioneer:2.0.0-RC1")
    }
}

testing {
    suites {
        // Configure the built-in test suite
        val test by getting(JvmTestSuite::class) {
            // Use JUnit Jupiter test framework
            useJUnitJupiter("5.9.1")
        }
    }
}
