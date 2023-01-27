/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws;

import clusterless.startup.Startup;
import clusterless.substrate.SubstrateProvider;
import clusterless.substrate.aws.cdk.*;
import clusterless.util.URIUtil;
import picocli.CommandLine;

/**
 *
 */
@CommandLine.Command(mixinStandardHelpOptions = true, subcommands = {
        Info.class,
        Verify.class,
        Diff.class,
        Deploy.class,
        Destroy.class,
        Synth.class,
        Report.class
})
public class Kernel extends Startup implements SubstrateProvider {
    public static void main(String[] args) {
        System.exit(new Kernel().execute(args));
    }

    @CommandLine.Option(names = "--cdk", description = "path to the cdk binary")
    public String cdk = "cdk";

    @CommandLine.Option(names = "--cdk-app", description = "path to the cdk json file")
    public String cdkApp = URIUtil.normalize("%s/bin/cls-aws".formatted(System.getProperty(Startup.CLUSTERLESS_HOME)));

    @CommandLine.Option(names = "--profile", description = "aws profile")
    public String profile = System.getenv("AWS_PROFILE");

    @CommandLine.Option(names = "--output", description = "cloud assembly file output")
    public String output = "cdk.out";

    public Kernel() {
    }

    @Override
    public String name() {
        return "aws";
    }

    @Override
    public int execute(String[] args) {
        return new CommandLine(this)
                .setCaseInsensitiveEnumValuesAllowed(true)
                .execute(args);
    }
}
