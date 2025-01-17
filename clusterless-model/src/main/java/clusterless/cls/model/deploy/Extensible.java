/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.model.deploy;

import clusterless.cls.json.ExtensibleResolver;
import clusterless.cls.json.JsonRequiredProperty;
import clusterless.cls.json.Views;
import clusterless.cls.model.Model;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 *
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true // allows the type property to be set
)
@JsonTypeIdResolver(ExtensibleResolver.class)
public abstract class Extensible extends Model {
    // can't use @JsonRequiredProperty here
    @JsonView(Views.Required.class)
    String type;
    @JsonRequiredProperty
    String name;
    boolean exclude = false;

    public Extensible() {
        type = resolveType(this);
    }

    /**
     * Without setting include = EXISTING_PROPERTY, Jackson will write `type` twice in the Json
     * <p>
     * It's a bit of a known issue, but not exactly documented.
     *
     * @param value
     * @return
     */
    private static String resolveType(Extensible value) {
        ExtensibleResolver extensibleResolver = new ExtensibleResolver();
        extensibleResolver.init(TypeFactory.defaultInstance().constructType(value.getClass()));
        return extensibleResolver.idFromValue(value);
    }

    private static Class<? extends Model> resolveModel(Extensible value) {
        ExtensibleResolver extensibleResolver = new ExtensibleResolver();
        extensibleResolver.init(TypeFactory.defaultInstance().constructType(value.getClass()));
        return extensibleResolver.modelClass(extensibleResolver.idFromValue(value));
    }

    public String type() {
        return type;
    }

    public String name() {
        return name;
    }

    /**
     * @return true if this object should be excluded when creating constructs
     */
    public boolean exclude() {
        return exclude;
    }

    public Class<? extends Model> modelType() {
        return resolveModel(this);
    }

    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    @Override
    public final boolean equals(Object obj) {
        return super.equals(obj);
    }
}
