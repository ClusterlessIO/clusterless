/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.command.project;


import picocli.CommandLine;

import java.util.Optional;

/**
 *
 */
@CommandLine.Command(
        hidden = true,
        description = "Import resources from the declared placement."
)
public class ImportCommandOptions extends ProjectCommandOptions {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @CommandLine.Option(
            names = "--approve",
            description = "Approve import of resources.",
            defaultValue = CommandLine.Option.NULL_VALUE,
            fallbackValue = "true",
            arity = "0..1"
    )
    Optional<Boolean> approve;

    public Optional<Boolean> approve() {
        return approve;
    }
}
