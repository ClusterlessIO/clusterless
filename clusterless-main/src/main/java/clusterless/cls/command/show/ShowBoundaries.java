/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.command.show;

import clusterless.cls.model.deploy.Boundary;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.util.Collection;

@CommandLine.Command(
        name = "boundary",
        description = "Show available provider boundaries."
)
public class ShowBoundaries extends ShowComponents {

    @Override
    protected @NotNull String elementSubType() {
        return "Boundaries";
    }

    @Override
    protected Collection<String> getNames() {
        return super.getNamesHaving(e -> Boundary.class.isAssignableFrom(e.getValue()));
    }
}
