/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.managed.component;

import clusterless.cls.managed.ModelType;
import clusterless.cls.model.Model;
import clusterless.cls.model.deploy.Arc;
import clusterless.cls.model.deploy.Deployable;
import clusterless.cls.model.deploy.Extensible;
import clusterless.cls.util.Annotations;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 *
 */
public class ComponentServices {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ComponentServices.class);

    public static final ComponentServices INSTANCE = new ComponentServices();

    private final EnumMap<Isolation, EnumMap<ModelType, Map<String, ComponentService<ComponentContext, Model, Component>>>> componentServices;

    {
        componentServices = new EnumMap<>(Isolation.class);
        Arrays.stream(Isolation.values()).forEach(k -> componentServices.put(k, new EnumMap<>(ModelType.class)));
    }

    protected ComponentServices() {
        var serviceLoader = ServiceLoader.load(ComponentService.class);

        serviceLoader.stream().forEach(s -> {
            Optional<DeclaresComponent> provides = Annotations.find(s.type(), DeclaresComponent.class);
            Optional<ProvidesComponent> typeOf = Annotations.find(s.type(), ProvidesComponent.class);
            Isolation isolation = provides.orElseThrow().isolation();
            ModelType model = provides.orElseThrow().provides();
            String type = typeOf.orElseThrow().type();

            LOG.info("loading component service provider: {} {} {}", isolation, model, type);

            ComponentService<ComponentContext, Model, Component> componentService = s.get();

            componentServices.get(isolation).computeIfAbsent(model, k -> new HashMap<>())
                    .put(type, componentService);
        });
    }

    public Map<String, ComponentService<ComponentContext, Model, Component>> componentServices() {
        Map<String, ComponentService<ComponentContext, Model, Component>> result = new LinkedHashMap<>();

        for (ModelType modelType : ModelType.values()) {
            result.putAll(componentServicesFor(modelType));
        }

        return result;
    }

    public Map<String, ComponentService<ComponentContext, Model, Component>> componentServicesFor(ModelType modelType) {
        Map<String, ComponentService<ComponentContext, Model, Component>> result = new HashMap<>();

        for (Isolation isolation : Isolation.values()) {
            if (componentServices.containsKey(isolation)) {
                if (componentServices.get(isolation).containsKey(modelType)) {
                    result.putAll(componentServices.get(isolation).get(modelType));
                }
            }
        }

        return result;
    }

    public EnumMap<ModelType, Map<String, ComponentService<ComponentContext, Model, Component>>> componentServicesFor(Isolation isolation) {
        return componentServices.get(isolation);
    }

    public Optional<ComponentService<ComponentContext, Model, Component>> componentServicesForArc(Arc<?> deployableModel) {
        return componentServiceFor(deployableModel, Isolation.managed, ModelType.Arc);
    }

    public Optional<ComponentService<ComponentContext, Model, Component>> componentServiceFor(Extensible extensible, Isolation isolation, ModelType modelType) {
        return Optional.ofNullable(componentServicesFor(isolation).get(modelType).get(extensible.type()));
    }

    public Map<Extensible, ComponentService<ComponentContext, Model, Component>> componentServicesFor(Isolation isolation, Deployable deployableModel, ModelType... modelTypes) {
        Map<Extensible, ComponentService<ComponentContext, Model, Component>> map = new LinkedHashMap<>();
        EnumMap<ModelType, Map<String, ComponentService<ComponentContext, Model, Component>>> containerMap = componentServicesFor(isolation);

        for (ModelType modelType : modelTypes) {
            if (!containerMap.containsKey(modelType)) {
                continue;
            }

            for (Extensible extensible : getExtensibleModelsFor(modelType, deployableModel)) {
                // put a null if not available
                map.put(extensible, containerMap.get(modelType).get(extensible.type()));
            }
        }

        return map;
    }

    private static List<Extensible> getExtensibleModelsFor(ModelType modelType, Deployable deployableModel) {
        return switch (modelType) {
            case Resource -> new ArrayList<>(deployableModel.resources());
            case Device -> new ArrayList<>(deployableModel.devices());
            case Activity -> new ArrayList<>(deployableModel.activities());
            case Boundary -> new ArrayList<>(deployableModel.boundaries());
            case Arc -> new ArrayList<>(deployableModel.arcs());
        };
    }
}
