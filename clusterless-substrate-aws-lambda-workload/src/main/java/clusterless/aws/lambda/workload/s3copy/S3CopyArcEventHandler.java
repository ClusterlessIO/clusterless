/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.aws.lambda.workload.s3copy;

import clusterless.aws.lambda.arc.ArcEventHandler;
import clusterless.aws.lambda.arc.ArcEventObserver;
import clusterless.aws.lambda.manifest.AttemptCounter;
import clusterless.aws.lambda.manifest.ManifestReader;
import clusterless.aws.lambda.manifest.ManifestWriter;
import clusterless.aws.lambda.util.PathMatcher;
import clusterless.cls.model.UriType;
import clusterless.cls.model.deploy.SinkDataset;
import clusterless.cls.model.manifest.Manifest;
import clusterless.cls.model.manifest.ManifestState;
import clusterless.cls.substrate.aws.event.ArcNotifyEvent;
import clusterless.cls.substrate.aws.event.ArcWorkloadContext;
import clusterless.cls.substrate.aws.sdk.ClientBase;
import clusterless.cls.substrate.aws.sdk.S3;
import clusterless.cls.util.Tuple2;
import clusterless.cls.util.Tuple3;
import clusterless.cls.util.URIs;
import com.amazonaws.services.lambda.runtime.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;
import java.util.*;

/**
 *
 */
public class S3CopyArcEventHandler extends ArcEventHandler<S3CopyProps> {
    private static final Logger LOG = LoggerFactory.getLogger(S3CopyArcEventHandler.class);

    protected static final S3 s3 = new S3();
    protected static final AttemptCounter attemptCounter = new AttemptCounter();

    protected ManifestReader manifestReader = new ManifestReader();
    protected Map<String, ManifestWriter> manifestWriters = ManifestWriter.writers(
            arcProps().sinks(),
            arcProps().sinkManifestTemplates(),
            UriType.identifier
    );
    protected PathMatcher.Builder pathMatcher = PathMatcher.builder()
            .withPathSeparator(arcProps().workloadProps().filter().pathSeparator())
            .withIgnoreCase(arcProps().workloadProps().filter().ignoreCase())
            .withIncludes(arcProps().workloadProps().filter().includes())
            .withExcludes(arcProps().workloadProps().filter().excludes());

    @Override
    protected Map<String, URI> handleEvent(ArcWorkloadContext arcWorkloadContext, Context context, ArcEventObserver eventObserver) {
        String fromRole = arcWorkloadContext.role();
        ArcNotifyEvent notifyEvent = arcWorkloadContext.arcNotifyEvent();
        String lotId = notifyEvent.lot();
        URI incomingManifestIdentifier = notifyEvent.manifest();

        Manifest incomingManifest = manifestReader.getManifest(incomingManifestIdentifier);

        eventObserver.applyFromManifest(incomingManifestIdentifier, incomingManifest);

        Map<String, URI> result = new LinkedHashMap<>();

        //  copy files
        URI fromDatasetPath = notifyEvent.dataset().pathURI();
        List<URI> fromUris = incomingManifest.uris();

        PathMatcher match = pathMatcher.withPath(fromDatasetPath.getPath())
                .build();

        for (Map.Entry<String, SinkDataset> sinkRoleEntry : arcProps().sinks().entrySet()) {
            String toRole = sinkRoleEntry.getKey();
            ManifestWriter manifestWriter = manifestWriters.get(toRole);
            if (incomingManifest.state() == ManifestState.empty) {
                LOG.info("manifest state empty, role: {} -> {}", fromRole, toRole);
                URI manifestURI = manifestWriter.writeEmptyManifest(lotId);
                result.put(toRole, manifestURI);

                eventObserver.applyToManifest(toRole, manifestURI);
                continue;
            }

            SinkDataset sinkDataset = sinkRoleEntry.getValue();

            eventObserver.applyToDataset(toRole, sinkDataset);

            URI toDatasetPath = sinkDataset.pathURI();

            // not using a map so that collisions can be managed independently on the to/from sides
            List<Tuple2<URI, URI>> toUris = new LinkedList<>();
            for (URI fromUri : fromUris) {

                if (!match.keep(fromUri.getPath())) {
                    continue;
                }

                URI toURI = URIs.fromTo(fromDatasetPath, fromUri, toDatasetPath);
                toUris.add(new Tuple2<>(fromUri, toURI));
            }

            // TODO: check integrity of uris to be copied

            List<URI> completed = new LinkedList<>();
            List<Tuple3<URI, URI, S3.Response>> failed = new LinkedList<>();

            int maxAllowedFailures = (int) Math.ceil(toUris.size() * workloadProperties().failArcOnPartialPercent());

            s3.copy(
                    toUris,
                    completed::add,
                    (tuple, response) -> {
                        failed.add(new Tuple3<>(tuple.get_1(), tuple.get_2(), response));

                        if (response.isAccessDenied()) {
                            return true;
                        }

                        return failed.size() > maxAllowedFailures;
                    }
            );

            URI manifestURI;

            if (!failed.isEmpty()) { // unintentional
                LOG.error("s3 object copy errors: {}, failed: {}", fromUris.size(), failed.size());

                Tuple3<URI, URI, ClientBase<S3Client>.Response> firstFailure = failed.get(0);
                if (firstFailure.get_3().isAccessDenied()) {
                    logErrorAndThrow(
                            RuntimeException::new,
                            firstFailure.get_3().exception(),
                            "bucket access denied, may not have access to either: {}, or: {}, confirm arc has permission to read from subscribed dataset buckets",
                            firstFailure.get_1().getHost(), // from bucket
                            firstFailure.get_2().getHost() // to bucket
                    );
                }

                Set<String> messages = new LinkedHashSet<>();
                for (Tuple3<URI, URI, S3.Response> failure : failed) {
                    messages.add(failure.get_3().exception().getMessage());
                    LOG.error("failed from: {}, to: {}, message: {}", failure.get_1(), failure.get_2(), failure.get_3().exception().getMessage(), failure.get_3().exception());
                }

                if (failed.size() > maxAllowedFailures) {
                    logErrorAndThrow(
                            RuntimeException::new,
                            "too many copy operations returned errors, failed: {}, is greater than allowed: {}, with message: {}",
                            failed.size(),
                            maxAllowedFailures,
                            messages.stream().findFirst().orElse("[no error message]")
                    );
                }

                List<String> errors = messages.stream().limit(3).toList();

                manifestURI = manifestWriter.writePartialManifest(
                        completed,
                        lotId,
                        attemptCounter.attemptId(context.getAwsRequestId()),
                        String.format("copy failed on role: %s, num: %d with messages: %s", toRole, failed.size(), errors)
                );
            } else if (completed.isEmpty()) { // intentional
                // if we allow for a filter predicate on the declaration, this will be a valid state
                LOG.info("no objects copied with no errors, role: {} -> {}, having: {}", fromRole, toRole, fromUris.size());
                manifestURI = manifestWriter.writeEmptyManifest(lotId);
            } else { // intentional
                LOG.info("successfully copied objects with no errors, role: {} -> {}, having: {}", fromRole, toRole, fromUris.size());
                manifestURI = manifestWriter.writeSuccessManifest(completed, lotId);
            }

            result.put(toRole, manifestURI);

            eventObserver.applyToManifest(toRole, manifestURI);
        }

        return result;
    }
}
