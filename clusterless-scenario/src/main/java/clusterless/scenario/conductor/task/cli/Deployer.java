/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.scenario.conductor.task.cli;

import java.nio.file.Path;
import java.util.List;

public class Deployer extends ClusterlessTask {

    public static final String CLS_PROJECT_DEPLOYER = "clsProjectDeployer";

    public Deployer(String taskDefName, Path workingDirectory, List<Path> projectFiles) {
        super(CLS_PROJECT_DEPLOYER, taskDefName, workingDirectory, projectFiles);
    }
}