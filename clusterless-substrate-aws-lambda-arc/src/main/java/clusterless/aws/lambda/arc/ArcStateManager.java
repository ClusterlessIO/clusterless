/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.aws.lambda.arc;

import clusterless.cls.json.JSONUtil;
import clusterless.cls.model.state.ArcState;
import clusterless.cls.substrate.aws.sdk.S3;
import clusterless.cls.substrate.uri.ArcStateURI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ArcStateManager {
    private static final Logger LOG = LoggerFactory.getLogger(ArcStateManager.class);
    private final S3 s3 = new S3();

    final ArcStateURI arcStatePath;

    public ArcStateManager(ArcStateURI arcStatePath) {
        Objects.requireNonNull(arcStatePath, "arcStatePath");
        this.arcStatePath = arcStatePath;

        if (this.arcStatePath.isIdentifier()) {
            throw new IllegalStateException("arc state path must be a path uri, got: " + arcStatePath.uri());
        }
    }

    /**
     * @param lotId
     * @return
     */
    public Optional<ArcState> setStateFor(String lotId, ArcState newState) {
        return setStateFor(lotId, newState, null);
    }

    public Optional<ArcState> setStateFor(String lotId, ArcState newState, Object payload) {
        Optional<ArcState> currentState = findStateFor(lotId);

        if (currentState.isPresent() && currentState.get() == newState) {
            throw new IllegalStateException("already in current state: " + newState);
        }

        LOG.info("found current state: {}", currentState.orElse(null));

        //  s3://state_bucket/{project}/{version}/{arc}/{lot}/{state}.txt
        URI currentStateIdentifier = currentState.map(s -> arcStatePath.withLot(lotId).withState(s).uri()).orElse(null);
        URI newStateIdentifier = arcStatePath.withLot(lotId).withState(newState).uri();

        LOG.info("setting arc state to: {}", newStateIdentifier);

        String body = payload == null ? "" : JSONUtil.writeAsStringSafe(payload);
        if (currentStateIdentifier == null) {
            s3.put(newStateIdentifier, "application/txt", body)
                    .isSuccessOrThrow(e -> new RuntimeException("unable to set state at: " + newStateIdentifier, e));
        } else if (payload != null) {
            s3.moveAppendLine(currentStateIdentifier, newStateIdentifier, body)
                    .isSuccessOrThrow(e -> new RuntimeException("unable to move state at: " + currentStateIdentifier + ", to: " + newStateIdentifier, e));
        } else {
            s3.move(currentStateIdentifier, newStateIdentifier)
                    .isSuccessOrThrow(e -> new RuntimeException("unable to move state at: " + currentStateIdentifier + ", to: " + newStateIdentifier, e));
        }

        return currentState;
    }

    public Optional<ArcState> findStateFor(String lotId) {
        //  s3://state_bucket/{project}/{version}/{arc}/{lot}/
        URI path = arcStatePath.withLot(lotId).uri();

        LOG.info("listing state paths for: {}", path);

        S3.Response response = s3.listPaths(path);

        if (!response.isSuccess()) {
            return Optional.empty();
        }

        List<String> paths = s3.listChildren(response);

        LinkedList<ArcState> states = paths.stream()
                .map(this::resolve)
                .collect(Collectors.toCollection(LinkedList::new));

        LOG.info("found states: {}", states);

        if (states.size() > 1) {
            throw new IllegalStateException("found more than one arc state: " + states);
        }

        if (states.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(states.getFirst());
    }

    private ArcState resolve(String s) {
        for (ArcState value : ArcState.values()) {
            if (s.contains(value.name())) {
                return value;
            }
        }
        return null;
    }
}
