/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws;

import clusterless.json.JSONUtil;
import clusterless.lambda.arc.ArcProps;
import clusterless.model.Model;
import clusterless.model.deploy.Extensible;
import clusterless.model.deploy.WorkloadProps;
import clusterless.substrate.aws.arc.s3copy.S3CopyArc;
import clusterless.substrate.aws.boundary.s3put.S3PutListenerBoundary;
import clusterless.substrate.aws.resource.batch.ComputeResource;
import clusterless.substrate.aws.resource.eventbridge.EventBridgeResource;
import clusterless.substrate.aws.resource.glue.database.GlueDatabaseResource;
import clusterless.substrate.aws.resource.glue.table.GlueTableResource;
import clusterless.substrate.aws.resource.s3.S3BucketResource;
import clusterless.util.Env;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * Confirms we can write out a valid json and read it back into an object
 */
public class JSONTest {
    @ParameterizedTest
    @ValueSource(classes = {
            S3PutListenerBoundary.class,
            S3CopyArc.class,
            EventBridgeResource.class,
            S3BucketResource.class,
            ComputeResource.class,
            GlueDatabaseResource.class,
            GlueTableResource.class
    })
    void serialize(Class<? extends Extensible> originalClass) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        Extensible original = originalClass.getConstructor().newInstance();

        Assertions.assertNotNull(original.type());
        Assertions.assertNotNull(original.modelType());

        String json = JSONUtil.writeAsPrettyStringSafe(original);

        Assertions.assertNotNull(json);

        Class<? extends Model> modelClass = original.modelType();

        Extensible copy = (Extensible) JSONUtil.readObjectSafe(json, modelClass);

        Assertions.assertEquals(original.getClass(), copy.getClass());
        Assertions.assertNotNull(copy.type());
        Assertions.assertNotNull(copy.modelType());
        Assertions.assertEquals(original.type(), copy.type());
    }

    /**
     * Confirms subclasses in the models will serialize properly into the remote lambda and retain class information
     * <p>
     * Not the workloadProps will be subclassed in all arcs, this accommodates that with additional overhead on the
     * json.
     */
    @Test
    void typeSerializeToEnv() {
        S3CopyArc arc = new S3CopyArc();

        ArcProps<WorkloadProps> arcProps = ArcProps.builder()
                .withSources(arc.sources())
                .withSinks(arc.sinks())
                .withSourceManifestPaths(null)
                .withSinkManifestTemplates(null)
                .withWorkloadProps(arc.workload().workloadProps()) // generally a subclass of WorkloadProps
                .build();

        Map<String, String> environment = Env.toEnv(arcProps);

        Env.fromEnv(
                environment::get,
                ArcProps.class,
                () -> ArcProps.builder().build()
        );
    }
}
