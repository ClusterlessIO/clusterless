/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

plugins {
    id("clusterless.java-application-conventions")
    id("io.github.chklauser.sjsonnet") version "1.0.0"
}

dependencies {
    implementation(project(":clusterless-common"))
    implementation(project(":clusterless-main-common"))
    implementation(project(":clusterless-substrate-aws-common")) {
        exclude("org.apache.logging.log4j")
        exclude("log4j:log4j")
        exclude("org.slf4j:slf4j-log4j12")
        exclude("org.apache.logging.log4j:log4j-slf4j-impl")
    }

    implementation("software.amazon.awssdk:s3")
    implementation("software.amazon.awssdk:eventbridge")
    implementation("software.amazon.awssdk:sqs")
    implementation("software.amazon.awssdk:glue")
    implementation("software.amazon.awssdk:athena")

    // the log4j exclusion works around the strict version requirement
    val conductor = "3.14.0"
    implementation("com.netflix.conductor:conductor-core:$conductor") {
        exclude("org.apache.logging.log4j")
        exclude("log4j:log4j")
        exclude("org.slf4j:slf4j-log4j12")
    }
    implementation("com.netflix.conductor:conductor-common:$conductor") {
        exclude("org.apache.logging.log4j")
        exclude("log4j:log4j")
        exclude("org.slf4j:slf4j-log4j12")
    }
    implementation("com.netflix.conductor:conductor-rest:$conductor") {
        exclude("org.apache.logging.log4j")
        exclude("log4j:log4j")
        exclude("org.slf4j:slf4j-log4j12")
    }
    implementation("com.netflix.conductor:conductor-client:$conductor") {
        exclude("org.apache.logging.log4j")
        exclude("log4j:log4j")
        exclude("org.slf4j:slf4j-log4j12")
    }
    implementation("com.netflix.conductor:conductor-redis-persistence:$conductor") {
        exclude("org.apache.logging.log4j")
        exclude("log4j:log4j")
        exclude("org.slf4j:slf4j-log4j12")
    }
    implementation("com.netflix.conductor:conductor-java-sdk:$conductor") {
        exclude("org.apache.logging.log4j")
        exclude("log4j:log4j")
        exclude("org.slf4j:slf4j-log4j12")
    }
    // java 17 drops it, this replaces it
    implementation("org.openjdk.nashorn:nashorn-core:15.4")

    implementation("javax.validation:validation-api:2.0.1.Final")

    val springBoot = "2.7.18"
    implementation("org.springframework.boot:spring-boot-starter:$springBoot") {
        exclude("org.springframework.boot", "spring-boot-starter-logging")
    }
    implementation("org.springframework.boot:spring-boot-starter-validation:$springBoot")
    implementation("org.springframework.boot:spring-boot-starter-web:$springBoot")
    implementation("org.springframework.boot:spring-boot-starter-actuator:$springBoot")
    implementation("org.springframework.retry:spring-retry:1.3.3") // 2.0.1 is on jdk 17

    runtimeOnly("org.glassfish.jaxb:jaxb-runtime:2.3.3")

    implementation("com.google.guava:guava")
}

// the current version of spring boot, used by conductor, hard codes a dependency on
// org.slf4j.impl.StaticLoggerBinder in the LogbackLoggingSystem class.
// this was removed in recent slf4j/logback releases so we must pin the scenario project
// to older versions of slf4j/logback
configurations.all {
    resolutionStrategy {
        force(
            "org.slf4j:slf4j-api:1.7.28",
            "ch.qos.logback:logback-classic:1.2.11",
            "ch.qos.logback:logback-core:1.2.11"
        )
    }
}

application {
    applicationName = "cls-scenario"
    mainClass.set("clusterless.scenario.Main")
}

idea {
    module {
        resourceDirs.add(file("src/main/cls"))
    }
}

sjsonnet {
    create("scenarios") {
        indent.set(2)
        sources.from(file("src/main/cls/scenarios"))
            .filter { f -> f.extension == "jsonnet" }
        externalVariables.putAll(
            properties.filter { p -> p.key.startsWith("scenario.") }.toMap()
        )
    }
}

val copyScenarios = tasks.register<Copy>("copyScenarios") {
    val jsonnet = tasks.named("jsonnetScenariosGenerate")
    dependsOn.add(jsonnet)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from("src/main/cls/scenarios") {
        exclude("**/*.jsonnet")
    }
    from(jsonnet)
    into(layout.buildDirectory.dir("scenarios"))
}

val run = tasks.named<JavaExec>("run") {
    dependsOn.add(copyScenarios)
    val mainInstall = tasks.getByPath(":clusterless-main:installDist") as Sync

    dependsOn.add(mainInstall)

//    jvmArgs = listOf("-Dlog4j.debug")

    args = listOf(
//        "--dry-run",
//        "--verify-on-dry-run",
//        "--server",
//        "localhost:8080",
//        "--disable-destroy",
        "--stop-on-failure",
        "--cls-app",
        "${mainInstall.destinationDir.absolutePath}/bin/cls",
        "-f",
        copyScenarios.get().destinationDir.absolutePath
    )
}

tasks.register("scenarios") {
    dependsOn.add(run)
}
