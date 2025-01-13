/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.managed.component;

import clusterless.cls.managed.ModelType;
import clusterless.cls.model.deploy.Device;

/**
 *
 */
@DeclaresComponent(
        provides = ModelType.Device,
        isolation = Isolation.managed
)
public interface DeviceComponentService<CC extends ComponentContext, M extends Device, C extends DeviceComponent> extends ComponentService<CC, M, C> {
}
