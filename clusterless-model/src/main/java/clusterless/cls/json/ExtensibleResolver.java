/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.json;

import clusterless.cls.managed.ModelType;
import clusterless.cls.managed.component.Component;
import clusterless.cls.managed.component.ComponentContext;
import clusterless.cls.managed.component.ComponentService;
import clusterless.cls.managed.component.ComponentServices;
import clusterless.cls.model.Model;
import clusterless.cls.model.deploy.Arc;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;

import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 */
public class ExtensibleResolver extends TypeIdResolverBase {

    private Map<String, ComponentService<ComponentContext, Model, Component>> serviceMap;
    private Map<Class<? extends Model>, String> reverseMap;

    public ExtensibleResolver() {
    }

    @Override
    public void init(JavaType javaType) {
        ComponentServices componentServices = ComponentServices.INSTANCE;

        Class<?> rawClass = javaType.getRawClass();
        ModelType modelType = ModelType.findFromModel(rawClass);

        if (modelType == null) {
            throw new IllegalStateException("unknown component type for: " + rawClass.getName());
        }

        serviceMap = componentServices.componentServicesFor(modelType);

        reverseMap = serviceMap.entrySet()
                .stream()
                .collect(Collectors.toMap(e -> e.getValue().modelClass(), Map.Entry::getKey));

        // allows for a model template to be generated for documentation
        reverseMap.put(Arc.class, "arc");
    }

    @Override
    public String idFromValue(Object value) {
        return reverseMap.get(value.getClass());
    }

    @Override
    public String idFromValueAndType(Object value, Class<?> suggestedType) {
        return null;
    }

    @Override
    public JsonTypeInfo.Id getMechanism() {
        return JsonTypeInfo.Id.NAME;
    }

    @Override
    public JavaType typeFromId(DatabindContext context, String id) {
        Class<Model> modelClass = modelClass(id);
        return context.getTypeFactory().constructType(modelClass);
    }

    public Class<Model> modelClass(String id) {
        ComponentService<ComponentContext, Model, Component> service = serviceMap.get(id);

        if (service == null) {
            throw new IllegalStateException("no service found for: " + id);
        }

        return service.modelClass();
    }
}
