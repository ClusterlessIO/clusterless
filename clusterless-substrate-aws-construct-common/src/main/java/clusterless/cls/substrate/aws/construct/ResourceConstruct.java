/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.construct;

import clusterless.cls.managed.component.ResourceComponent;
import clusterless.cls.model.deploy.Resource;
import clusterless.cls.substrate.aws.managed.ManagedComponentContext;
import clusterless.commons.naming.Label;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public class ResourceConstruct<M extends Resource> extends ExtensibleConstruct<M> implements ResourceComponent {
    public ResourceConstruct(@NotNull ManagedComponentContext context, @NotNull M model, @NotNull Label discriminator) {
        super(context, model, discriminator);
    }
}
