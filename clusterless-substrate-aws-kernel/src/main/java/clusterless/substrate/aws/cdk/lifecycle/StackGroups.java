/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.cdk.lifecycle;

import clusterless.managed.component.ModelType;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class StackGroups {
    @NotNull
    public List<ModelType[]> isolatableStackGroups() {
        List<ModelType[]> stackGroups = new LinkedList<>();

        // all values will result in one stack, but this controls the order of the stacks
        stackGroups.add(ModelType.values(ModelType.Resource));
        stackGroups.add(ModelType.values(ModelType.Boundary));

        return stackGroups;
    }

    public List<ModelType[]> includableStackGroups() {
        List<ModelType[]> stackGroups = new LinkedList<>();

        // places these types in the same stack
        stackGroups.add(ModelType.values(ModelType.Resource, ModelType.Boundary));

        return stackGroups;
    }

    public List<ModelType[]> embeddedStackGroups() {
        List<ModelType[]> stackGroups = new LinkedList<>();

        // each of these is embedded in the parent type stack
        stackGroups.add(ModelType.values(ModelType.Workload));

        return stackGroups;
    }
}