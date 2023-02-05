/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.bootstrap;

import clusterless.command.BootstrapCommandOptions;
import clusterless.substrate.aws.ProcessExec;
import clusterless.util.Label;
import clusterless.util.Lists;
import clusterless.util.OrderedSafeMaps;
import picocli.CommandLine;
import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * cdk assets bucket:
 * cdk-hnb659fds-assets-086903124729-us-west-2
 * Bucket Versioning
 * Enabled
 * <p>
 * ecr assets bucket
 * cdk-hnb659fds-container-assets-086903124729-us-west-2
 */
@CommandLine.Command(
        name = "bootstrap"
)
public class Bootstrap implements Callable<Integer> {
    @CommandLine.Mixin
    ProcessExec processExec = new ProcessExec();

    @CommandLine.Mixin
    BootstrapCommandOptions commandOptions = new BootstrapCommandOptions();

    @Override
    public Integer call() throws Exception {
        if (commandOptions.synth()) {
            return synth();
        }
        return exec();
    }

    private Integer exec() {
        String account = prompt(commandOptions.account(), "Enter AWS account id to bootstrap: ");
        String region = prompt(commandOptions.region(), "Enter region to bootstrap: ");

        List<String> args = Lists.list(OrderedSafeMaps.of(
                "--account",
                account,
                "--region",
                region
        ));

        String appArgs = "--synth %s".formatted(String.join(" ", args));

        processExec.setUseTempOutput(true);
        processExec.executeCDKApp("deploy", "bootstrap", appArgs);

        return 0;
    }

    private Integer synth() {
        App app = new App();

        String stackName = Label.of("ClusterlessBootstrap").lowerHyphen();

        StackProps stackProps = StackProps.builder()
                .stackName(stackName)
                .description("This stack includes resources needed to manage Clusterless projects in this environment")
                .env(Environment.builder()
                        .account(commandOptions.account())
                        .region(commandOptions.region())
                        .build())
                .build();

        new BootstrapStack(app, "ClusterlessBootstrapStack", stackProps);

        app.synth();

        return 0;
    }

    private String prompt(String value, String fmt) {
        if (value == null && System.console() != null) {
            return System.console().readLine(fmt);
        }

        return value;
    }
}
