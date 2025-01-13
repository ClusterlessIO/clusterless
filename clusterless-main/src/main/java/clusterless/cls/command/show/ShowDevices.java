/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.command.show;

import clusterless.cls.model.deploy.Device;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.util.Collection;

@CommandLine.Command(
        name = "device",
        description = "Show available provider devices."
)
public class ShowDevices extends ShowComponents {
    @Override
    protected @NotNull String elementSubType() {
        return "Devices";
    }

    @Override
    protected Collection<String> getNames() {
        return super.getNamesHaving(e -> Device.class.isAssignableFrom(e.getValue()));
    }
}
