/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.aws.lambda.boundary.frequents3put;

import clusterless.aws.lambda.EventHandler;
import clusterless.aws.lambda.arc.ArcNotifyEventPublisher;
import clusterless.aws.lambda.manifest.ManifestWriter;
import clusterless.aws.lambda.transform.json.event.AWSEvent;
import clusterless.aws.lambda.util.PathMatcher;
import clusterless.cls.model.UriType;
import clusterless.cls.substrate.aws.sdk.S3;
import clusterless.cls.substrate.aws.sdk.SQS;
import clusterless.cls.util.Env;
import clusterless.commons.temporal.IntervalBuilder;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import com.amazonaws.services.lambda.runtime.serialization.PojoSerializer;
import com.amazonaws.services.lambda.runtime.serialization.events.LambdaEventSerializers;
import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.MessageSystemAttributeName;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static software.amazon.awssdk.utils.StringUtils.isEmpty;

public class FrequentPutEventBoundaryHandler extends EventHandler<AWSEvent, FrequentPutEventBoundaryObserver> {
    private static final Logger LOG = LoggerFactory.getLogger(FrequentPutEventBoundaryHandler.class);
    public static final PojoSerializer<S3Event> serializer = LambdaEventSerializers.serializerFor(S3Event.class, S3Event.class.getClassLoader());
    protected final SQS sqs = new SQS();
    protected final FrequentS3PutBoundaryProps transformProps = Env.fromEnv(
            FrequentS3PutBoundaryProps.class,
            () -> FrequentS3PutBoundaryProps.builder()
                    .build()
    );

    protected final IntervalBuilder intervalBuilder = new IntervalBuilder(transformProps.lotUnit());

    protected ManifestWriter manifestWriter = new ManifestWriter(
            transformProps.manifestCompletePath(),
            UriType.identifier
    );

    protected ArcNotifyEventPublisher arcNotifyEventPublisher = new ArcNotifyEventPublisher(
            transformProps.eventBusName(),
            transformProps.dataset()
    );

    protected PathMatcher pathMatcher = PathMatcher.builder()
            .withPath(transformProps.dataset().pathURI().getPath())
            .withPathSeparator(transformProps.filter().pathSeparator())
            .withIgnoreCase(transformProps.filter().ignoreCase())
            .withIncludes(transformProps.filter().includes())
            .withExcludes(transformProps.filter().excludes())
            .build();

    public FrequentPutEventBoundaryHandler() {
        super(AWSEvent.class);
    }

    protected FrequentPutEventBoundaryObserver observer() {
        return new FrequentPutEventBoundaryObserver() {
            @Override
            public void applyLotId(String lotId) {
                LOG.info("using lot: {}", lotId);
            }

            @Override
            public void applyDatasetItemsSize(int datasetItemsSize) {
                LOG.info("dataset items size: {}", datasetItemsSize);
            }

            @Override
            public void applyManifestURI(URI manifestURI) {
                LOG.info("using manifest uri: {}", manifestURI);
            }

            @Override
            public void applyEvent(OffsetDateTime time) {
                LOG.info("received, time: {}", time);
            }
        };
    }

    @Override
    public void handleEvent(AWSEvent event, Context context, FrequentPutEventBoundaryObserver eventObserver) {
        OffsetDateTime scheduledTime = event.getTime();

        eventObserver.applyEvent(scheduledTime);

        // skip any message that happened in the current lot interval
        // a small attempt to maintain flow control
        Instant filterTime = intervalBuilder.truncate(scheduledTime.toInstant());

        // use the lot previous to the current event time
        // this roughly accumulates events that occurred during the previous period
        Instant lotTime = intervalBuilder.previous(filterTime);

        String lotId = intervalBuilder.format(lotTime);

        eventObserver.applyLotId(lotId);

        SQS.Response urlResponse = sqs.queueUrl(transformProps.sqsQueueName());

        urlResponse.isSuccessOrThrowRuntime(
                r -> String.format("unable to retrieve from queue url: %s, %s", transformProps.sqsQueueName(), r.errorMessage())
        );

        String queueUrl = sqs.queueUrl(urlResponse);

        LOG.info("using queue: {}", queueUrl);

        Set<String> skipped = new LinkedHashSet<>();
        List<URI> uris = new LinkedList<>();

        Stopwatch getStopwatch = Stopwatch.createUnstarted();
        Stopwatch deleteStopwatch = Stopwatch.createUnstarted();

        while (true) {
            getStopwatch.start();
            SQS.Response messagesResponse = sqs.get(queueUrl, transformProps.sqsWaitTimeSeconds());
            getStopwatch.stop();

            messagesResponse.isSuccessOrThrowRuntime(
                    r -> String.format("unable to retrieve messages from queue: %s, %s", queueUrl, r.errorMessage())
            );

            List<Message> messages = sqs.get(messagesResponse);

            // if we have previously skipped the message, skip it again
            // note original messages object is unmodifiable
            if (!skipped.isEmpty()) {
                messages = messages.stream()
                        .filter(m -> !skipped.contains(m.messageId()))
                        .collect(Collectors.toList());
            }

            LOG.info("received new messages: {}", messages.size());

            if (messages.isEmpty()) {
                break;
            }

            List<String> deleteMessages = new LinkedList<>();

            for (Message message : messages) {
                String sentTimestamp = message.attributes().get(MessageSystemAttributeName.SENT_TIMESTAMP);
                Instant messageSentTime = Instant.ofEpochMilli(Long.parseLong(sentTimestamp));

                if (messageSentTime.isAfter(filterTime)) {
                    skipped.add(message.messageId());
                    LOG.info("{}, message sent time: {}, filter time: {}, skipping", message.messageId(), messageSentTime, filterTime);
                    continue;
                }

                deleteMessages.add(message.receiptHandle());

                S3Event s3Event = serializer.fromJson(message.body());
                List<S3EventNotification.S3EventNotificationRecord> records = s3Event.getRecords();

                LOG.info("{}, received records: {}", message.messageId(), records.size());

                records.stream()
                        .map(S3EventNotification.S3EventNotificationRecord::getS3)
                        .map(this::uriFromDetail)
                        .filter(uri -> !isEmpty(uri.getPath()) && uri.getPath().charAt(uri.getPath().length() - 1) != '/') // only retain files
                        .peek(eventObserver::applyIdentifierURI)
                        .filter(pathMatcher::keep)
                        .forEach(uris::add);
            }

            LOG.info("deleting messages: {}", deleteMessages.size());

            if (!deleteMessages.isEmpty()) {
                deleteStopwatch.start();
                sqs.delete(queueUrl, deleteMessages).isSuccessOrThrowRuntime(
                        r -> String.format("unable to delete messages from queue: %s, %s", queueUrl, r.errorMessage())
                );
                deleteStopwatch.stop();
            }
        }

        Duration getElapsed = getStopwatch.elapsed();
        Duration deleteElapsed = deleteStopwatch.elapsed();
        LOG.info("durations for, get: {}, delete: {}, total: {}", getElapsed, deleteElapsed, getElapsed.plus(deleteElapsed));

        eventObserver.applyDatasetItemsSize(uris.size());

        URI manifestURI = uris.isEmpty() ?
                manifestWriter.writeEmptyManifest(lotId) : manifestWriter.writeSuccessManifest(uris, lotId);

        eventObserver.applyManifestURI(manifestURI);

        arcNotifyEventPublisher.publishEvent(lotId, manifestURI);
    }

    private URI uriFromDetail(S3EventNotification.S3Entity event) {
        String bucket = event.getBucket().getName();
        String key = event.getObject().getKey();
        return S3.createS3URI(bucket, key);
    }
}
