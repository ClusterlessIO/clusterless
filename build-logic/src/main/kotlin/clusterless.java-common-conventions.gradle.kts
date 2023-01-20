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
    constraints {
        // manage dependency versions here
        implementation("org.slf4j:slf4j-api:2.0.6")
        implementation("org.slf4j:slf4j-simple:2.0.6")

        implementation("com.google.guava:guava:31.1-jre")

        implementation("com.fasterxml.jackson.core:jackson-core:2.14.1")
        implementation("com.fasterxml.jackson.core:jackson-databind:2.14.1")
        implementation("com.fasterxml.jackson.datatype:jackson-datatype-joda:2.14.1")
        implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.14.1")
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
