/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws;

import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "info"
)
public class Info implements Callable<Integer> {

    @CommandLine.Mixin
    ProcessExec processExec = new ProcessExec();

    public Info() {

    }

    @CommandLine.Command(name = "version")
    public Integer version() {
        return processExec.executeCDK("--version");
    }

    @CommandLine.Command(name = "which")
    public Integer which() {
        return processExec.executeProcess("which", processExec.cdk());
    }

    @Override
    public Integer call() throws Exception {
        return 0;
    }
}
