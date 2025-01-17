/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.resource.eventbridge;

import clusterless.cls.managed.component.ResourceComponent;
import clusterless.cls.substrate.aws.construct.ExtensibleConstruct;
import clusterless.cls.substrate.aws.managed.ManagedComponentContext;
import clusterless.commons.naming.Label;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.services.events.EventBus;

/**
 *
 */
public class EventBridgeResourceConstruct extends ExtensibleConstruct<EventBridgeResource> implements ResourceComponent {

    private final EventBus eventBus;

    public EventBridgeResourceConstruct(@NotNull ManagedComponentContext context, @NotNull EventBridgeResource model) {
        super(context, model, Label.of(model.eventBusName()));

        eventBus = EventBus.Builder.create(this, Label.of(model.eventBusName()).camelCase())
                .eventBusName(model.eventBusName())
                .build();

        exportArnRefFor(model(), eventBus(), eventBus().getEventBusArn(), "event bus arn");
        exportNameRefFor(model(), eventBus(), model().eventBusName(), "event bus name");
    }

    public EventBus eventBus() {
        return eventBus;
    }
}
