/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.cdk.lifecycle;


import clusterless.cls.command.project.DestroyCommandOptions;
import clusterless.cls.substrate.aws.cdk.BaseCDKCommand;
import clusterless.cls.substrate.aws.cdk.CDKCommand;
import clusterless.cls.substrate.aws.cdk.CDKProcessExec;
import clusterless.cls.substrate.aws.meta.Metadata;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "destroy"
)
public class Destroy extends BaseCDKCommand implements Callable<Integer> {
    @CommandLine.Mixin
    DestroyCommandOptions commandOptions = new DestroyCommandOptions();
    @CommandLine.Mixin
    CDKProcessExec processExec = new CDKProcessExec(commandOptions::dryRun, commandOptions::retry, this::verbosityLevel, commandOptions::profile);

    @Override
    public Integer call() throws Exception {
        Integer exitCode = processExec.executeLifecycleProcess(
                getCommonConfig(),
                getProviderConfig(),
                commandOptions,
                CDKCommand.DESTROY,
                getRequireDestroyApproval(commandOptions.approve().orElse(null))
        );

        if (exitCode != 0) {
            return exitCode;
        }

        return Metadata.removeDeployablesMetadata(processExec.getOutputPath(), commandOptions.dryRun());
    }
}
