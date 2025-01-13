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
    implementation(project(":clusterless-common"))
    implementation(project(":clusterless-model"))
    implementation(project(":clusterless-substrate-aws-construct-common"))

    // here the constructs and lambda implementations intersect
    // this is relatively brittle as the lambda classes are hard coded strings
    implementation(project(":clusterless-substrate-aws-lambda-common-model"))
    implementation(project(":clusterless-substrate-aws-lambda-transform-model"))
    implementation(project(":clusterless-substrate-aws-lambda-workload-model"))
}
