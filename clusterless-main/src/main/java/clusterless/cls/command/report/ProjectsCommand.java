/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.command.report;

import clusterless.cls.command.CommandWrapper;
import picocli.CommandLine;

@CommandLine.Command(
        name = "projects",
        description = "List all deployed projects."
)
public class ProjectsCommand extends CommandWrapper<ProjectsCommandOptions> {
    public ProjectsCommand() {
        super(new ProjectsCommandOptions());
    }
}
