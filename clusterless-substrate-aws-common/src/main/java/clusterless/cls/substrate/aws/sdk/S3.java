/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.sdk;

import clusterless.cls.json.JSONUtil;
import clusterless.cls.util.Tuple2;
import clusterless.cls.util.URIs;
import com.google.common.collect.Iterables;
import org.jetbrains.annotations.NotNull;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;

import java.net.URI;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 *
 */
public class S3 extends ClientBase<S3Client> {

    public static final int MAX_PAYLOAD = 1024 * 1024;

    public static URI createS3URI(String bucket, String key) {
        return URIs.create("s3", bucket, key);
    }

    private int maxKeys = 1000;

    public S3() {
    }

    public S3(String profile) {
        super(profile);
    }

    public S3(String profile, int maxKeys) {
        super(profile);
        this.maxKeys = maxKeys;
    }

    public S3(String profile, String region) {
        super(profile, region);
    }

    @NotNull
    protected String getEndpointEnvVar() {
        return "AWS_S3_ENDPOINT";
    }

    @Override
    protected S3Client createClient(String region) {
        logEndpointOverride();

        return S3Client.builder()
                .region(region == null ? null : Region.of(region)) // allows sdk to lookup region in chain
                .credentialsProvider(credentialsProvider)
                .endpointOverride(endpointOverride)
                .build();
    }

    public Response list() {
        ListBucketsRequest request = ListBucketsRequest.builder()
                .build();

        try (S3Client client = createClient(region)) {
            return new Response(client.listBuckets(request));
        } catch (Exception exception) {
            return new Response(exception);
        }
    }

    public List<String> list(Response response) {
        if (hasNoAwsResponse(response)) {
            return Collections.emptyList();
        }

        ListBucketsResponse awsResponse = (ListBucketsResponse) response.awsResponse();

        if (awsResponse.hasBuckets()) {
            return awsResponse.buckets()
                    .stream()
                    .map(Bucket::name)
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    public Response exists(String bucketName) {
        return exists(region, bucketName);
    }

    public Response exists(String region, String bucketName) {
        Objects.requireNonNull(region, "region");
        Objects.requireNonNull(bucketName, "bucketName");

        HeadBucketRequest request = HeadBucketRequest.builder()
                .bucket(bucketName)
                .build();

        try (S3Client client = createClient(region)) {
            return new Response(client.headBucket(request));
        } catch (Exception exception) {
            return new Response(exception);
        }
    }

    public Response exists(URI location) {
        return exists(region, location);
    }

    public Response exists(String region, URI identifier) {
        Objects.requireNonNull(region, "region");
        Objects.requireNonNull(identifier, "identifier");

        HeadObjectRequest request = HeadObjectRequest.builder()
                .bucket(identifier.getHost())
                .key(URIs.asKey(identifier))
                .build();

        try (S3Client client = createClient(region)) {
            return new Response(client.headObject(request));
        } catch (Exception exception) {
            return new Response(exception);
        }
    }

    public boolean exists(Response response) {
        if (hasNoAwsResponse(response)) {
            return false;
        }

        if (response.sdkHttpResponse == null) {
            throw new IllegalStateException("sdk http response is null");
        }

        return response.sdkHttpResponse.isSuccessful();
    }

    public Response create(String bucketName) {
        return create(region, bucketName);
    }

    public Response create(String region, String bucketName) {
        Objects.requireNonNull(region, "region");
        Objects.requireNonNull(bucketName, "bucketName");

        CreateBucketRequest request = CreateBucketRequest.builder()
                .bucket(bucketName)
                .build();

        try (S3Client client = createClient(region)) {
            return new Response(client.createBucket(request));
        } catch (Exception exception) {
            return new Response(exception);
        }
    }

    public Response put(URI identifier, String contentType, ByteBuffer byteBuffer) {
        Objects.requireNonNull(identifier, "identifier");
        Objects.requireNonNull(contentType, "contentType");
        Objects.requireNonNull(byteBuffer, "byteBuffer");

        RequestBody requestBody = RequestBody.fromByteBuffer(byteBuffer);

        return put(identifier, contentType, requestBody);
    }

    public Response put(URI identifier, String contentType, Object value) {
        Objects.requireNonNull(identifier, "identifier");
        Objects.requireNonNull(contentType, "contentType");
        Objects.requireNonNull(value, "value");

        String body = JSONUtil.writeAsStringSafe(value);

        return put(identifier, contentType, body);
    }

    public Response put(URI identifier, String contentType, String body) {
        Objects.requireNonNull(identifier, "identifier");
        Objects.requireNonNull(contentType, "contentType");
        Objects.requireNonNull(body, "body");

        RequestBody requestBody = body.isEmpty() ? RequestBody.empty() : RequestBody.fromString(body);

        return put(identifier, contentType, requestBody);
    }

    @NotNull
    private ClientBase<S3Client>.Response put(URI identifier, String contentType, RequestBody requestBody) {
        String bucketName = identifier.getHost();
        String key = URIs.asKey(identifier);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

        try (S3Client client = createClient()) {
            return new Response(client.putObject(putObjectRequest, requestBody));
        } catch (Exception exception) {
            return new Response(exception);
        }
    }

    public Response get(URI identifier) {
        Objects.requireNonNull(identifier, "identifier");

        String bucketName = identifier.getHost();
        String key = URIs.asKey(identifier);

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        try (S3Client client = createClient()) {
            return new Response(client.getObjectAsBytes(getObjectRequest));
        } catch (Exception exception) {
            return new Response(exception);
        }
    }

    public Response remove(URI identifier) {
        Objects.requireNonNull(identifier, "identifier");

        try (S3Client client = createClient()) {
            return remove(client, identifier);
        }
    }

    private S3.Response remove(S3Client client, URI identifier) {
        try {
            return new Response(client.deleteObject(createDeleteRequest(identifier)));
        } catch (Exception exception) {
            return new Response(exception);
        }
    }

    private static DeleteObjectRequest createDeleteRequest(URI identifier) {
        String bucketName = identifier.getHost();
        String key = URIs.asKey(identifier);

        return DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
    }

    public Response moveAppendLine(URI fromIdentifier, URI toIdentifier, String append) {
        Objects.requireNonNull(fromIdentifier, "fromIdentifier");
        Objects.requireNonNull(toIdentifier, "toIdentifier");
        Objects.requireNonNull(append, "append");

        try (S3Client client = createClient()) {
            Response getResponse = get(fromIdentifier);

            if (!getResponse.isSuccess()) {
                return getResponse;
            }

            if (getResponse.asGetObjectResponse().contentLength() < MAX_PAYLOAD) {
                ByteBuffer byteBuffer = getResponse.asByteBuffer();
                String newLine = byteBuffer.capacity() != 0 ? "\n" : "";
                ByteBuffer buffer = ByteBuffer.allocate(byteBuffer.capacity() + append.length() + newLine.length());
                buffer.put(byteBuffer);
                buffer.put(newLine.getBytes());
                buffer.put(append.getBytes());
                String contentType = getResponse.asGetObjectResponse().contentType();
                Response putResponse = put(toIdentifier, contentType, buffer);

                if (!putResponse.isSuccess()) {
                    return putResponse;
                }
            } else {
                ClientBase<S3Client>.Response response = copy(client, fromIdentifier, toIdentifier);

                if (!response.isSuccess()) {
                    return response;
                }
            }

            return remove(client, fromIdentifier);
        }
    }

    public Response move(URI fromIdentifier, URI toIdentifier) {
        Objects.requireNonNull(fromIdentifier, "fromIdentifier");
        Objects.requireNonNull(toIdentifier, "toIdentifier");

        try (S3Client client = createClient()) {
            ClientBase<S3Client>.Response response = copy(client, fromIdentifier, toIdentifier);

            if (!response.isSuccess()) {
                return response;
            }

            return remove(client, fromIdentifier);
        }
    }

    public Response listPaths(URI path) {
        return list(path, "/");
    }

    public Response listObjects(URI path) {
        return list(path, null);
    }

    public Response listThisOrChildObjects(URI path) {
        Objects.requireNonNull(path, "path");

        String bucketName = path.getHost();
        String key = URIs.asKey(path); // foo

        return list(null, bucketName, key);
    }

    protected Response list(URI path, String delimiter) {
        Objects.requireNonNull(path, "path");

        String bucketName = path.getHost();
        String key = URIs.asKeyPath(path); // foo/

        return list(delimiter, bucketName, key);
    }

    @NotNull
    private Response list(String delimiter, String bucketName, String key) {
        ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(key)
                .delimiter(delimiter)

                .maxKeys(maxKeys)
                .build();

        try (S3Client client = createClient()) {
            return new Response(client.listObjectsV2(listObjectsV2Request));
        } catch (Exception exception) {
            return new Response(exception);
        }
    }

    public Responses listObjectsIterable(URI path, URI startExclusive) {
        return listIterable(path, null, startExclusive);
    }

    public Iterable<Response> listPathsIterable(URI path) {
        return listIterable(path, "/", null);
    }

    public Iterable<Response> listObjectsIterable(URI path) {
        return listIterable(path, null, null);
    }

    public Iterable<Response> listThisOrChildObjectsIterable(URI path) {
        Objects.requireNonNull(path, "path");

        String bucketName = path.getHost();
        String key = URIs.asKey(path); // foo

        return listIterable(null, bucketName, key, null);
    }

    protected Responses listIterable(URI path, String delimiter, URI startAfter) {
        Objects.requireNonNull(path, "path");

        String bucketName = path.getHost();
        String key = URIs.asKeyPath(path); // foo/
        String startKey = URIs.asKey(startAfter); // foo/bar

        return listIterable(delimiter, bucketName, key, startKey);
    }

    @NotNull
    private Responses listIterable(String delimiter, String bucketName, String key, String startAfter) {
        ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .startAfter(startAfter)
                .prefix(key)
                .delimiter(delimiter)

                .maxKeys(maxKeys)
                .build();

        S3Client client = createClient();
        try {
            ListObjectsV2Iterable awsResponse = client.listObjectsV2Paginator(listObjectsV2Request);
            return new Responses(client) {
                @NotNull
                @Override
                public Iterator<Response> iterator() {
                    return Iterables.transform(awsResponse, Response::new).iterator();
                }
            };
        } catch (Exception exception) {
            client.close();
            return new Responses() {
                @NotNull
                @Override
                public Iterator<Response> iterator() {
                    return Collections.singletonList(new Response(exception)).iterator();
                }
            };
        }
    }

    public Stream<String> listChildrenStream(Iterable<Response> responses, URI endExclusive, String objectName, Consumer<ClientBase<S3Client>.Response> handler) {
        String end = URIs.asKey(endExclusive);
        return StreamSupport.stream(responses.spliterator(), false)
                .takeWhile(r -> handle(r, handler))
                .flatMap(this::listChildrenStream)
                .filter(key -> key.endsWith(objectName)) // only return objects, that directories
                .takeWhile(key -> !key.startsWith(end));
    }

    private boolean handle(Response r, Consumer<ClientBase<S3Client>.Response> handler) {
        if (r.isSuccess()) {
            return true;
        }

        handler.accept(r);

        return false;
    }

    public List<String> listChildren(Response response) {
        return listChildrenStream(response).toList();
    }

    public Stream<String> listChildrenStream(Response response) {
        if (hasNoAwsResponse(response)) {
            return Stream.empty();
        }

        ListObjectsV2Response awsResponse = (ListObjectsV2Response) response.awsResponse();

        if (awsResponse.hasCommonPrefixes() && !awsResponse.commonPrefixes().isEmpty()) {
            return awsResponse.commonPrefixes()
                    .stream()
                    .map(CommonPrefix::prefix);
        }

        if (awsResponse.hasContents()) {
            return awsResponse.contents()
                    .stream()
                    .map(S3Object::key);
        }

        return Stream.empty();
    }


    /**
     * Has a limit of 5G objects,
     *
     * @param from
     * @param to
     * @return
     */
    public Response copy(URI from, URI to) {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");

        try (S3Client client = createClient()) {
            return copy(client, from, to);
        }
    }

    public boolean copy(List<Tuple2<URI, URI>> toUris, Consumer<URI> success, BiFunction<Tuple2<URI, URI>, Response, Boolean> isFailure) {
        Objects.requireNonNull(toUris, "toUris");

        try (S3Client client = createClient()) {
            for (Tuple2<URI, URI> tuple : toUris) {
                URI from = tuple.get_1();
                URI to = tuple.get_2();

                // TODO: apply retry logic here. note response knows about throttling and retryable exceptions
                S3.Response response = copy(client, from, to);

                if (response.isSuccess()) {
                    success.accept(to);
                } else {
                    // stop on true
                    if (isFailure.apply(tuple, response)) {
                        return false; // failure
                    }
                }
            }
        }
        return true; // success
    }

    private Response copy(S3Client client, URI from, URI to) {
        try {
            return new Response(client.copyObject(createCopyRequest(from, to)));
        } catch (Exception exception) {
            return new Response(exception);
        }
    }

    private static CopyObjectRequest createCopyRequest(URI from, URI to) {
        String fromBucket = from.getHost();
        String fromKey = URIs.asKey(from);
        String toBucket = to.getHost();
        String toKey = URIs.asKey(to);

        return CopyObjectRequest.builder()
                .sourceBucket(fromBucket)
                .sourceKey(fromKey)
                .destinationBucket(toBucket)
                .destinationKey(toKey)
                .build();
    }

    public Instant lastModified(Response response) {
        return ((HeadObjectResponse) response.awsResponse).lastModified();
    }

    private static boolean hasNoAwsResponse(S3.Response response) {
        verifyResponse(response);

        if (response.exception instanceof NoSuchBucketException || response.exception instanceof NoSuchKeyException) {
            return true;
        }

        if (response.exception == null) {
            return response.awsResponse == null;
        }

        return true;
    }
}
