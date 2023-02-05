/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.lambda.transform;

import clusterless.lambda.transform.json.AWSEvent;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

/**
 *
 */
public class PutEventTransformHandler implements RequestHandler<AWSEvent, ArcNotifyEvent> {
    @Override
    public ArcNotifyEvent handleRequest(AWSEvent input, Context context) {

        return null;
    }
}
