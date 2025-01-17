/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.util;

import clusterless.commons.naming.Partition;
import com.google.common.base.Strings;

import javax.annotation.Nullable;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 *
 */
public class URIs {
    private static final Pattern NORMALIZE = Pattern.compile("(?<!:)/{2,}");

    public static URI create(String scheme, String authority, String path) {
        try {
            return new URI(scheme, authority, normalize("/", path), null, null);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("unable to create uri", e);
        }
    }

    public static URI copyAppendAsPath(URI uri, String... path) {
        return copyAppend(
                uri,
                Partition.NULL
                        .having(path)
                        .partition(true) // #path will prepend a "/"
        );
    }

    public static URI copyAppend(URI uri, String... path) {
        if (path.length == 0) {
            return uri;
        }

        try {
            String normalize = normalize(
                    Partition.of(uri.getPath())
                            .having(path)
                            .partition(path[path.length - 1].endsWith("/"))
            );
            return new URI(uri.getScheme(), uri.getAuthority(), normalize, null, null);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("unable to copy uri", e);
        }
    }

    public static URI copyWith(URI uri, String path) {
        if (Strings.isNullOrEmpty(path)) {
            path = "/";
        } else if (path.charAt(0) != '/') {
            path = "/" + path;
        }

        try {
            return new URI(uri.getScheme(), uri.getAuthority(), path, null, null);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("unable to copy uri", e);
        }
    }

    public static URI normalizeURI(@Nullable URI uri) {
        if (uri == null) {
            return null;
        }

        String normalize = normalize(uri.getPath());

        return copyWith(uri, normalize);
    }

    /**
     * Returns a normalized path, that is multiple slashes and dots are removed.
     * <p>
     * Unlike Path#normalize, the last slash is retained.
     *
     * @param path
     * @return
     */
    public static String normalize(String path) {
        String empty = Strings.nullToEmpty(path);
        return NORMALIZE.matcher(empty).replaceAll("/");
    }

    public static String normalize(String path, String append) {
        if (append == null || append.isEmpty()) {
            return normalize(path);
        }

        return normalize(path, new String[]{append});
    }

    public static String normalize(String path, String... appends) {
        return normalize(
                Partition.of(path)
                        .having(appends)
                        .partition()
        );
    }

    /**
     * Returns the path part of the uri without the leading slash, but with a trailing slash, for use in S3 request.
     *
     * @param uri
     * @return
     */
    public static String asKeyPath(URI uri) {
        String key = asKey(uri);

        if (key == null) {
            return null;
        }

        if (key.charAt(key.length() - 1) == '/') {
            return key;
        }

        return key.concat("/");
    }

    /**
     * Returns the path part of the uri without the leading slash, for use in S3 request.
     *
     * @param uri
     * @return null if the path is empty
     */
    public static String asKey(URI uri) {
        if (uri == null) {
            return null;
        }

        String normalize = uri.normalize().getPath();

        return asKey(normalize);
    }

    /**
     * Returns the path part of the uri without the leading slash, for use in S3 request.
     *
     * @param path
     * @return null if the path is empty
     */
    public static String asKey(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }

        if (path.charAt(0) == '/') {
            if (path.length() == 1) {
                return null;
            }

            return path.substring(1);
        }

        return path;
    }

    public static String asKeyPrefix(URI path) {
        if (path == null) {
            return null;
        }

        return asKeyPrefix(path.getPath());
    }

    /**
     * Removes the first slash and trailing slash, if any.
     *
     * @param path the path to remove the first and trailing slash from
     * @return the path without the first and trailing slash
     */
    public static String asKeyPrefix(String path) {
        path = asKey(path);

        if (path == null || path.isEmpty()) {
            return null;
        }

        if (path.charAt(path.length() - 1) == '/') {
            return path.substring(0, path.length() - 1);
        }

        return path;
    }

    public static URI fromTo(URI fromBase, URI from, URI toBase) {
        String fromBaseScheme = fromBase.getScheme();
        String fromScheme = from.getScheme();
        if (!fromBaseScheme.equals(fromScheme)) {
            throw new IllegalArgumentException(String.format("fromBase and from must have the same scheme, got fromBase: %s, from: %s", fromBaseScheme, fromScheme));

        }

        String fromBaseHost = fromBase.getHost();
        String fromHost = from.getHost();
        if (!fromBaseHost.equals(fromHost)) {
            throw new IllegalArgumentException(String.format("fromBase and from must have the same host, got fromBase: %s, from: %s", fromBaseHost, fromHost));
        }

        String fromBasePath = normalize(fromBase.getPath(), "/");
        String fromPath = normalize(from.getPath());
        if (!fromPath.startsWith(fromBasePath)) {
            throw new IllegalArgumentException(String.format("fromBase and from must have a common path, got fromBase: %s, from: %s", fromBasePath, fromPath));
        }

        return copyAppend(toBase, fromPath.substring(fromBasePath.length()));
    }

    public static String encodeOnly(String chars, String path) {
        if (chars.isEmpty()) {
            return path;
        }

        for (String c : chars.split("")) {
            String encode = URLEncoder.encode(c, UTF_8);
            path = path.replace(c, encode);
        }
        return path;
    }
}
