/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.util.stream.Stream;

/**
 *
 */
public class URIsTest {

    public static final String IS_NULL = null;

    @Test
    void normalize() {
        Assertions.assertEquals("/", URIs.normalize("/"));
        Assertions.assertEquals("/", URIs.normalize("//"));
        Assertions.assertEquals("foo/", URIs.normalize("foo//"));
        Assertions.assertEquals("/foo/", URIs.normalize("/foo//"));
        Assertions.assertEquals("/foo/", URIs.normalize("/foo/"));
        Assertions.assertEquals("", URIs.normalize(IS_NULL));
    }

    @Test
    void normalizeAppend() {
        Assertions.assertEquals("/", URIs.normalize("/", IS_NULL));
        Assertions.assertEquals("/", URIs.normalize("//", IS_NULL));

        // this form is safe for requiring a root slash when generating a URI path
        Assertions.assertEquals("/foo/", URIs.normalize("/", "foo//"));
        Assertions.assertEquals("/foo/", URIs.normalize("/", "/foo//"));

        Assertions.assertEquals("/bar/foo/", URIs.normalize("/bar", "foo//"));
        Assertions.assertEquals("/bar/foo/", URIs.normalize("/bar", "/foo//"));

        Assertions.assertEquals("foo/", URIs.normalize("foo//", ""));
        Assertions.assertEquals("foo/bar", URIs.normalize("foo//", "bar"));
        Assertions.assertEquals("foo/bar/", URIs.normalize("foo//", "bar/"));
        Assertions.assertEquals("/foo/", URIs.normalize("/foo//"));
        Assertions.assertEquals("/foo/bar/", URIs.normalize("/foo/", "bar//"));

        Assertions.assertEquals("foo/bar", URIs.normalize("foo//", "/bar"));
        Assertions.assertEquals("foo/bar/", URIs.normalize("foo//", "/bar/"));
        Assertions.assertEquals("/foo/bar/", URIs.normalize("/foo/", "/bar//"));

        Assertions.assertEquals("", URIs.normalize(IS_NULL, IS_NULL));
    }

    @Test
    void normalizeURI() {
        Assertions.assertEquals(URI.create("s3://bucket/"), URIs.normalizeURI(URI.create("s3://bucket/")));
        Assertions.assertEquals(URI.create("s3://bucket/"), URIs.normalizeURI(URI.create("s3://bucket//")));
        Assertions.assertEquals(URI.create("s3://bucket/foo/"), URIs.normalizeURI(URI.create("s3://bucket/foo//")));
        Assertions.assertEquals(URI.create("s3://bucket/foo/"), URIs.normalizeURI(URI.create("s3://bucket/foo/")));
        Assertions.assertEquals(URI.create("s3://bucket/foo/bar"), URIs.normalizeURI(URI.create("s3://bucket/foo//bar")));
        Assertions.assertEquals(URI.create("s3://bucket/foo/bar"), URIs.normalizeURI(URI.create("s3://bucket/foo/bar")));
        Assertions.assertEquals(URI.create("s3://bucket/foo/bar/"), URIs.normalizeURI(URI.create("s3://bucket/foo//bar//")));
        Assertions.assertEquals(URI.create("s3://bucket/foo/bar/"), URIs.normalizeURI(URI.create("s3://bucket/foo/bar/")));
    }

    @Test
    void prefix() {
        Assertions.assertEquals("foo", URIs.asKeyPath(URI.create("/foo")));
        Assertions.assertEquals("foo/", URIs.asKeyPath(URI.create("/foo/")));
        Assertions.assertEquals("foo/", URIs.asKeyPath(URI.create("/foo//")));
        Assertions.assertEquals("foo/", URIs.asKeyPath(URI.create("s3://bucket/foo//")));
        Assertions.assertEquals("foo/", URIs.asKeyPath(URI.create("s3://bucket//foo//")));
        Assertions.assertNull(URIs.asKeyPath(URI.create("/")));
        Assertions.assertNull(URIs.asKeyPath(URI.create("/")));
        Assertions.assertNull(URIs.asKeyPath(URI.create("s3://bucket")));
        Assertions.assertNull(URIs.asKeyPath(URI.create("s3://bucket/")));
    }

    public static Stream<Arguments> copyArguments() {
        return Stream.of(
                Arguments.arguments(
                        "s3://from/path/",
                        "s3://from/path/one/two/three/file.txt",
                        "s3://to/path1/path2/",
                        "s3://to/path1/path2/one/two/three/file.txt"
                ),
                Arguments.arguments(
                        "s3://from/path",
                        "s3://from/path/one/two/three/file.txt",
                        "s3://to/path1/path2",
                        "s3://to/path1/path2/one/two/three/file.txt"
                ),
                Arguments.arguments(
                        "s3://from/path/",
                        "s3://from/path/one/two/three/file.txt",
                        "s3://to/path1/path2",
                        "s3://to/path1/path2/one/two/three/file.txt"
                ),
                Arguments.arguments(
                        "s3://from/path",
                        "s3://from/path/one/two/three/file.txt",
                        "s3://to/path1/path2/",
                        "s3://to/path1/path2/one/two/three/file.txt"
                )
        );
    }

    @ParameterizedTest
    @MethodSource("copyArguments")
    void copy(String fromBaseString, String fromString, String toBaseString, String toString) {
        URI fromBase = URI.create(fromBaseString);
        URI from = URI.create(fromString);
        URI toBase = URI.create(toBaseString);
        URI expectedTo = URI.create(toString);
        URI actualTo = URIs.fromTo(fromBase, from, toBase);

        Assertions.assertEquals(expectedTo, actualTo);
    }
}
