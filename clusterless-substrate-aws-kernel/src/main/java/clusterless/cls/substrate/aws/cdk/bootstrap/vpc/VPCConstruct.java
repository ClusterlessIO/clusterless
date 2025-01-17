/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.cdk.bootstrap.vpc;

import clusterless.cls.substrate.aws.managed.Managed;
import clusterless.cls.substrate.aws.resources.Vpcs;
import clusterless.commons.substrate.aws.cdk.naming.ResourceNames;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awscdk.services.ec2.IpAddresses;
import software.amazon.awscdk.services.ec2.SubnetConfiguration;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.ec2.Vpc;
import software.constructs.Construct;

import java.util.Collections;

public class VPCConstruct extends Construct implements Managed {
    private static final Logger LOG = LoggerFactory.getLogger(VPCConstruct.class);

    private final Vpc vpc;
    private final String vpcName;
    private String cidrBlock = "10.14.0.0/16";
    private int cidrMask = 19; // 8k hosts per subnet

    public VPCConstruct(@NotNull Construct scope) {
        super(scope, "BootstrapVpc");

        vpcName = Vpcs.bootstrapVPCName(this);

        LOG.info("creating vpc with name: {}, cidrBlock: {}, cidrMask: {}", vpcName, cidrBlock, cidrMask);

        vpc = Vpc.Builder.create(this, "Vpc")
                .vpcName(vpcName)
                .ipAddresses(IpAddresses.cidr(cidrBlock))
                .maxAzs(99) // use all AZs, some regions have more than 3
                .enableDnsHostnames(true)
                .enableDnsSupport(true)
                .subnetConfiguration(Collections.singletonList(
                        SubnetConfiguration.builder()
                                .name(ResourceNames.regionUniqueName(this, "ClusterlessSubnet"))
                                // PUBLIC grants access to the internet, need to confirm Batch still requires this to work
                                // PRIVATE_WITH_EGRESS will create a NAT gateway which has a charge
                                .subnetType(SubnetType.PUBLIC)
                                .mapPublicIpOnLaunch(false)
                                .cidrMask(cidrMask)
                                .build()
                ))
                .build();
    }

    public Vpc vpc() {
        return vpc;
    }

    public String vpcArn() {
        return vpc.getVpcArn();
    }

    public String vpcId() {
        return vpc.getVpcId();
    }

    public String vpcName() {
        return vpcName;
    }
}
