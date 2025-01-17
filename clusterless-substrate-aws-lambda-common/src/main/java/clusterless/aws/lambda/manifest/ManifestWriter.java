/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.aws.lambda.manifest;

import clusterless.cls.model.UriType;
import clusterless.cls.model.deploy.SinkDataset;
import clusterless.cls.model.manifest.Manifest;
import clusterless.cls.model.manifest.ManifestState;
import clusterless.cls.substrate.aws.sdk.S3;
import clusterless.cls.substrate.uri.ManifestURI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManifestWriter {
    private static final Logger LOG = LoggerFactory.getLogger(ManifestWriter.class);

    private final S3 s3 = new S3();
    private final ManifestURI sinkManifestPath;
    private final UriType uriType;

    public static Map<String, ManifestWriter> writers(Map<String, SinkDataset> sinks, Map<String, ManifestURI> sinkManifestPaths, UriType uriType) {
        Map<String, ManifestWriter> results = new HashMap<>();

        for (String role : sinks.keySet()) {
            ManifestURI sinkManifestPath = sinkManifestPaths.get(role);
            results.put(role, new ManifestWriter(sinkManifestPath, uriType));
        }

        return results;
    }

    public ManifestWriter(ManifestURI sinkManifestPath, UriType uriType) {
        this.sinkManifestPath = sinkManifestPath;
        this.uriType = uriType;
    }

    public URI writeSuccessManifest(List<URI> uris, String lotId) {
        return writeManifest(uris, lotId, sinkManifestPath, ManifestState.complete, null, null);
    }

    public URI writePartialManifest(List<URI> uris, String lotId, String attempt, String comment) {
        return writeManifest(uris, lotId, sinkManifestPath, ManifestState.partial, attempt, comment);
    }

    public URI writeEmptyManifest(String lotId) {
        return writeManifest(Collections.emptyList(), lotId, sinkManifestPath, ManifestState.empty, null, null);
    }

    private URI writeManifest(List<URI> uris, String lotId, ManifestURI sinkManifestPath, ManifestState state, String attempt, String comment) {
        Manifest manifest = Manifest.builder()
                .withState(state)
                .withComment(comment)
                .withLotId(lotId)
                .withUriType(uriType)
                .withUris(uris)
                .build();

        // put manifest, nested under the 'lot' partition
        URI sinkManifestIdentifier = sinkManifestPath
                .withState(state)
                .withLot(lotId)
                .withAttemptId(attempt)
                .uri();

        LOG.info("testing manifest: {}", sinkManifestIdentifier);

        // TODO: perform a listing to test for states (completed, empty, etc)
        S3.Response exists = s3.exists(sinkManifestIdentifier);

        exists.isNotSuccessOrThrow(
                r -> String.format("manifest already exists: %s", sinkManifestIdentifier),
                ManifestExistsException::new
        );

        LOG.atInfo()
                .setMessage("writing {} to path: {}")
                .addArgument(() -> manifest.getClass().getSimpleName())
                .addArgument(() -> sinkManifestIdentifier)
                .log();

        S3.Response response = s3.put(sinkManifestIdentifier, manifest.contentType(), manifest);

        response.isSuccessOrThrowRuntime(
                r -> String.format("unable to write object: %s, %s", sinkManifestIdentifier, r.errorMessage())
        );

        return sinkManifestIdentifier;
    }
}
