/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.aws.lambda.arc;

import clusterless.aws.lambda.StreamResultHandler;
import clusterless.cls.model.state.ArcState;
import clusterless.cls.substrate.aws.event.ArcNotifyEvent;
import clusterless.cls.substrate.aws.event.ArcStateContext;
import clusterless.cls.substrate.aws.event.ArcWorkloadContext;
import clusterless.cls.util.Env;
import com.amazonaws.services.lambda.runtime.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Converts an ArcNotifyEvent into an ArcStateContext for use within the state machine.
 */
public class ArcStateStartHandler extends StreamResultHandler<ArcNotifyEvent, ArcStateContext> {
    private static final Logger LOG = LoggerFactory.getLogger(ArcStateStartHandler.class);
    protected static final ArcStateProps arcStateProps = Env.fromEnv(
            ArcStateProps.class,
            () -> ArcStateProps.builder()
                    .build()
    );

    ArcStateManager arcStateManager = new ArcStateManager(arcStateProps.arcStatePath());

    protected ArcStateStartHandler(ArcStateManager arcStateManager) {
        this();
        this.arcStateManager = arcStateManager;
    }

    public ArcStateStartHandler() {
        super(ArcNotifyEvent.class, ArcStateContext.class);
    }

    protected ArcStateStartObserver observer() {
        return new ArcStateStartObserver() {
            @Override
            public void applyCurrentState(String lotId, ArcState currentState) {

            }

            @Override
            public void applyFinalArcStates(ArcState previous, ArcState current) {

            }

            @Override
            public void applyRoles(List<String> roles) {

            }
        };
    }

    @Override
    public ArcStateContext handleRequest(ArcNotifyEvent event, Context context) {
        logInfoObject("incoming arc event: {}", event);

        ArcStateContext arcStateContext = handleEvent(event, context, observer());

        logInfoObject("outgoing arc context: {}", arcStateContext);

        return arcStateContext;
    }

    protected ArcStateContext handleEvent(ArcNotifyEvent event, Context context, ArcStateStartObserver eventObserver) {
        String lotId = event.lot();

        // get arc state
        Optional<ArcState> currentState = arcStateManager.findStateFor(lotId);

        eventObserver.applyCurrentState(lotId, currentState.orElse(null));

        // if already running, punt back up to the state machine
        if (currentState.isPresent() && currentState.get() == ArcState.running) {
            eventObserver.applyFinalArcStates(ArcState.running, ArcState.running);
            LOG.info("lot already running: {}", lotId);
            return ArcStateContext.builder()
                    .withArcWorkloadContext(ArcWorkloadContext.builder()
                            .withArcNotifyEvent(event)
                            .build())
                    .withPreviousState(ArcState.running)
                    .withCurrentState(ArcState.running)
                    .build();
        }

        // if already completed, punt back up to the state machine
        if (currentState.isPresent() && (currentState.get() == ArcState.complete)) {
            LOG.info("lot already completed: {}", lotId);
            eventObserver.applyFinalArcStates(currentState.get(), currentState.get());
            return ArcStateContext.builder()
                    .withArcWorkloadContext(ArcWorkloadContext.builder()
                            .withArcNotifyEvent(event)
                            .build())
                    .withPreviousState(currentState.get())
                    .withCurrentState(currentState.get())
                    .build();
        }

        if (currentState.isPresent() && currentState.get() == ArcState.partial) {
            LOG.info("lot was partially completed: {}", lotId);
            // TODO: get partial manifests and apply to the arc context
        }

        // set to running
        Optional<ArcState> previousState = arcStateManager.setStateFor(lotId, ArcState.running);

        // confirm there isn't some race condition
        // TODO: create new exception to capture in state machine
        if (!currentState.equals(previousState)) {
            logErrorAndThrow(IllegalStateException::new, "unexpected state change from: {}, to: {}", currentState.orElse(null), previousState.orElse(null));
        }

        List<String> roles = arcStateProps.sources().entrySet()
                .stream()
                .filter(e -> e.getValue().sameDataset(event.dataset()))
                .map(Map.Entry::getKey).collect(Collectors.toList());

        eventObserver.applyRoles(roles);

        if (roles.size() == 0) {
            logErrorAndThrow(IllegalStateException::new, "no role found for: {}", event.dataset());
        }

        if (roles.size() != 1) {
            logErrorAndThrow(IllegalStateException::new, "too many roles found for: {}, got: {}", event.dataset(), roles);
        }

        // embed notify event
        // create sink manifests identifiers
        // list existing sink partial manifest identifiers
        eventObserver.applyFinalArcStates(currentState.orElse(null), ArcState.running);
        return ArcStateContext.builder()
                .withArcWorkloadContext(ArcWorkloadContext.builder()
                        .withArcNotifyEvent(event)
                        .withRole(roles.get(0))
                        .build())
                .withPreviousState(currentState.orElse(null)) // partial or null
                .withCurrentState(ArcState.running)
                .build();
    }
}
