/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.resources;

import clusterless.naming.Ref;
import clusterless.naming.Stage;
import clusterless.substrate.aws.managed.ManagedProject;
import clusterless.substrate.aws.managed.StagedApp;
import software.amazon.awscdk.Fn;
import software.constructs.Construct;

import java.util.Objects;
import java.util.Optional;

public class Refs {

    public static Optional<String> resolveArn(Construct scope, String refValue) {
        Objects.requireNonNull(refValue, "value must not be null");

        Optional<String> arn = arnFor(refValue);

        if (arn.isPresent()) {
            return arn;
        }

        Stage stage = StagedApp.stagedOf(scope).stage();
        ManagedProject managedProject = ManagedProject.projectOf(scope);

        String[] split = refValue.split(":");

        if (split.length != 3) {
            throw new IllegalStateException("unknown reference type: " + refValue);
        }

        String resourceNs = split[0];
        String resourceType = split[1];
        String resourceName = split[2];

        String ref = Ref.arnRef()
                .withProvider("aws")
                .withStage(stage)
                .withScope(managedProject.name())
                .withScopeVersion(managedProject.version())
                .withResourceNs(resourceNs)
                .withResourceType(resourceType)
                .withResourceName(resourceName)
                .exportName();

        return Optional.of(Fn.importValue(ref));
    }

    public static Optional<String> arnFor(String value) {
        Objects.requireNonNull(value, "value must not be null");

        if (Ref.isRef(value)) {
            return Optional.of(Fn.importValue(value));
        }

        if (value.startsWith("arn:")) {
            return Optional.of(value);
        }

        return Optional.empty();
    }
}
