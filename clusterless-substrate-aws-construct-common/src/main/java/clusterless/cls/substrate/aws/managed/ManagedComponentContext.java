/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.managed;

import clusterless.cls.config.Configurations;
import clusterless.cls.managed.component.ComponentContext;
import clusterless.cls.managed.dataset.DatasetResolver;
import clusterless.cls.model.deploy.Deployable;
import software.constructs.Construct;

/**
 *
 */
public class ManagedComponentContext implements ComponentContext {

    final Configurations configurations;
    final DatasetResolver resolver;
    final ManagedApp managedApp;
    final Deployable deployable;
    final Managed parent;

    public ManagedComponentContext(Configurations configurations, DatasetResolver resolver, ManagedApp managedApp, Deployable deployable) {
        this(configurations, resolver, managedApp, deployable, managedApp);
    }

    public ManagedComponentContext(Configurations configurations, DatasetResolver resolver, ManagedApp managedApp, Deployable deployable, Managed parent) {
        this.configurations = configurations;
        this.resolver = resolver;
        this.managedApp = managedApp;
        this.deployable = deployable;
        this.parent = parent;
    }

    public Configurations configurations() {
        return configurations;
    }

    public DatasetResolver resolver() {
        return resolver;
    }

    public ManagedApp managedApp() {
        return managedApp;
    }

    public Deployable deployable() {
        return deployable;
    }

    public Construct parentConstruct() {
        return parent.asConstruct();
    }
}
