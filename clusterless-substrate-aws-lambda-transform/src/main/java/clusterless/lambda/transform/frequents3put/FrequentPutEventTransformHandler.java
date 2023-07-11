/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.lambda.transform.frequents3put;

import clusterless.lambda.EventHandler;
import clusterless.lambda.arc.ArcNotifyEventPublisher;
import clusterless.lambda.manifest.ManifestWriter;
import clusterless.lambda.transform.json.event.AWSEvent;
import clusterless.model.UriType;
import clusterless.substrate.aws.sdk.S3;
import clusterless.substrate.aws.sdk.SQS;
import clusterless.temporal.IntervalBuilder;
import clusterless.util.Env;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import com.amazonaws.services.lambda.runtime.serialization.PojoSerializer;
import com.amazonaws.services.lambda.runtime.serialization.events.LambdaEventSerializers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.MessageSystemAttributeName;

import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static software.amazon.awssdk.utils.StringUtils.isEmpty;

public class FrequentPutEventTransformHandler extends EventHandler<AWSEvent, FrequentPutEventTransformObserver> {
    private static final Logger LOG = LogManager.getLogger(FrequentPutEventTransformHandler.class);
    public static final PojoSerializer<S3Event> serializer = LambdaEventSerializers.serializerFor(S3Event.class, S3Event.class.getClassLoader());
    protected final SQS sqs = new SQS();
    protected final FrequentS3PutTransformProps transformProps = Env.fromEnv(
            FrequentS3PutTransformProps.class,
            () -> FrequentS3PutTransformProps.builder()
                    .build()
    );

    protected final IntervalBuilder intervalBuilder = new IntervalBuilder(transformProps.lotUnit());

    protected ManifestWriter manifestWriter = new ManifestWriter(
            transformProps.manifestCompletePath(),
            transformProps.dataset(),
            UriType.identifier
    );

    protected ArcNotifyEventPublisher arcNotifyEventPublisher = new ArcNotifyEventPublisher(
            transformProps.eventBusName(),
            transformProps.dataset()
    );

    public FrequentPutEventTransformHandler() {
        super(AWSEvent.class);
    }

    protected FrequentPutEventTransformObserver observer() {
        return new FrequentPutEventTransformObserver() {
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
    public void handleEvent(AWSEvent event, Context context, FrequentPutEventTransformObserver eventObserver) {
        OffsetDateTime scheduledTime = event.getTime();

        eventObserver.applyEvent(scheduledTime);

        // skip any message that happened in the current lot interval
        // a small attempt to maintain flow control
        Instant filterTime = intervalBuilder.truncate(scheduledTime).toInstant();

        // use the lot previous to the current event time
        // this roughly accumulates events that occurred during the previous period
        Instant lotTime = intervalBuilder.previous(intervalBuilder.truncate(scheduledTime.toInstant()));

        String lotId = intervalBuilder.format(lotTime);

        eventObserver.applyLotId(lotId);

        SQS.Response urlResponse = sqs.queueUrl(transformProps.sqsQueueName());

        urlResponse.isSuccessOrThrowRuntime(
                r -> String.format("unable to retrieve from queue url: %s, %s", transformProps.sqsQueueName(), r.errorMessage())
        );

        String queueUrl = sqs.queueUrl(urlResponse);

        Set<String> skipped = new LinkedHashSet<>();
        List<URI> uris = new LinkedList<>();

        while (true) {
            SQS.Response messagesResponse = sqs.get(queueUrl, 20);

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

            LOG.info("from: {}, received new messages: {}", queueUrl, messages.size());

            if (messages.isEmpty()) {
                break;
            }

            List<String> deleteMessages = new LinkedList<>();

            for (Message message : messages) {
                String sentTimestamp = message.attributes().get(MessageSystemAttributeName.SENT_TIMESTAMP);
                Instant messageSentTime = Instant.ofEpochMilli(Long.parseLong(sentTimestamp));

                if (messageSentTime.isAfter(filterTime)) {
                    skipped.add(message.messageId());
                    LOG.info("from: {}:{}, message sent time: {}, filter time: {}, skipping", queueUrl, message.messageId(), messageSentTime, filterTime);
                    continue;
                }

                deleteMessages.add(message.receiptHandle());

                S3Event s3Event = serializer.fromJson(message.body());
                List<S3EventNotification.S3EventNotificationRecord> records = s3Event.getRecords();

                LOG.info("from: {}:{}, received records: {}", queueUrl, message.messageId(), records.size());

                records.stream()
                        .map(S3EventNotification.S3EventNotificationRecord::getS3)
                        .map(this::uriFromDetail)
                        .filter(uri -> !isEmpty(uri.getPath()) && uri.getPath().charAt(uri.getPath().length() - 1) != '/') // only retain files
                        .peek(eventObserver::applyIdentifierURI)
                        .forEach(uris::add);
            }

            LOG.info("from: {}, deleting messages: {}", queueUrl, deleteMessages.size());

            if (!deleteMessages.isEmpty()) {
                sqs.delete(queueUrl, deleteMessages).isSuccessOrThrowRuntime(
                        r -> String.format("unable to delete messages from queue: %s, %s", queueUrl, r.errorMessage())
                );
            }
        }

        eventObserver.applyDatasetItemsSize(uris.size());

        URI manifestURI = uris.isEmpty() ?
                manifestWriter.writeEmptyManifest(uris, lotId) : manifestWriter.writeSuccessManifest(uris, lotId);

        eventObserver.applyManifestURI(manifestURI);

        arcNotifyEventPublisher.publishEvent(lotId, manifestURI);
    }

    private URI uriFromDetail(S3EventNotification.S3Entity event) {
        String bucket = event.getBucket().getName();
        String key = event.getObject().getKey();
        return S3.createS3URI(bucket, key);
    }
}