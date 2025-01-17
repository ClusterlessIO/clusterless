/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.model.deploy;

import clusterless.cls.json.JsonRequiredProperty;
import clusterless.cls.managed.component.DocumentsModel;
import clusterless.cls.model.Struct;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 *
 */
@DocumentsModel(
        synopsis = "The project descriptor.",
        description = """
                Names the project to be deployed

                name: The name of the project. Required.
                                
                version: The version of the project. Required.
                         It's recommended to use a date for the version, such as "20230101".
                """
)
@JsonPropertyOrder({"name", "version"})
public class Project implements Struct {
    public static Project create(String value) {
        requireNonNull(value, "value");

        String[] split = value.split(":");

        if (split.length == 1) {
            return new Project(split[0], null);
        }

        return new Project(split[0], split[1]);
    }

    @JsonRequiredProperty
    String name;
    @JsonRequiredProperty
    String version;

    public Project() {
    }

    public Project(String name, String version) {
        this.name = name;
        this.version = version;
    }

    private Project(Builder builder) {
        name = builder.name;
        version = builder.version;
    }


    public String name() {
        return name;
    }

    public String version() {
        return version;
    }

    @JsonIgnore
    public String id() {
        return String.format("%s/%s", name(), version());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return Objects.equals(name, project.name) && Objects.equals(version, project.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Project{");
        sb.append("name='").append(name).append('\'');
        sb.append(", version='").append(version).append('\'');
        sb.append('}');
        return sb.toString();
    }

    /**
     * {@code Project} builder static inner class.
     */
    public static final class Builder {
        private String name;
        private String version;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        /**
         * Sets the {@code name} and returns a reference to this Builder enabling method chaining.
         *
         * @param val the {@code name} to set
         * @return a reference to this Builder
         */
        public Builder withName(String val) {
            name = val;
            return this;
        }

        /**
         * Sets the {@code version} and returns a reference to this Builder enabling method chaining.
         *
         * @param val the {@code version} to set
         * @return a reference to this Builder
         */
        public Builder withVersion(String val) {
            version = val;
            return this;
        }

        /**
         * Returns a {@code Project} built from the parameters previously set.
         *
         * @return a {@code Project} built with parameters of this {@code Project.Builder}
         */
        public Project build() {
            return new Project(this);
        }
    }
}
