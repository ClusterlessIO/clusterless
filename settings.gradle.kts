/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

pluginManagement {
    // Include 'plugins build' to define convention plugins.
    includeBuild("build-logic")
}

rootProject.name = "clusterless"

include("clusterless-model")
include("clusterless-main-common")
include("clusterless-main")

include("clusterless-substrate-aws-kernel")

include("clusterless-substrate-aws-construct-common")
include("clusterless-substrate-aws-construct-core")

include("clusterless-substrate-aws-service-transform")
