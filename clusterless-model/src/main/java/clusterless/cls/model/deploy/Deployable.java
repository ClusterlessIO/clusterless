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
import clusterless.cls.model.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@DocumentsModel(
        synopsis = "A deployable project, the base of a project file",
        description = """
                Use this model to define a deployable project.
                                
                > cls show model --model deployable > project.json
                                
                A project is a collection of resources, activities, boundaries, barriers, and arcs deployed into
                a providers placement environment.
                """
)
public class Deployable extends Model {

    public static final String PROVIDER_POINTER = "/placement/provider";

    @JsonIgnore
    File sourceFile;

    @JsonRequiredProperty
    Project project = new Project();
    @JsonRequiredProperty
    Placement placement = new Placement();
    @JsonProperty("resources")
    List<Resource> resources = new ArrayList<>();
    @JsonProperty("devices")
    List<Device> devices = new ArrayList<>();
    @JsonProperty("activities")
    List<Activity> activities = new ArrayList<>();
    @JsonProperty("boundaries")
    List<Boundary> boundaries = new ArrayList<>();
    @JsonProperty("barriers")
    List<Barrier> barriers = new ArrayList<>();
    @JsonProperty("arcs")
    List<Arc<? extends Workload>> arcs = new ArrayList<>();

    public Deployable() {
    }

    public File sourceFile() {
        return sourceFile;
    }

    public void setSourceFile(File sourceFile) {
        this.sourceFile = sourceFile;
    }

    public Project project() {
        return project;
    }

    public Placement placement() {
        return placement;
    }

    public List<Resource> resources() {
        return resources;
    }

    public List<Device> devices() {
        return devices;
    }

    public List<Activity> activities() {
        return activities;
    }

    public List<Boundary> boundaries() {
        return boundaries;
    }

    public List<Barrier> barriers() {
        return barriers;
    }

    public List<Arc<? extends Workload>> arcs() {
        return arcs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Deployable that = (Deployable) o;
        return Objects.equals(sourceFile, that.sourceFile) && Objects.equals(project, that.project) && Objects.equals(placement, that.placement) && Objects.equals(resources, that.resources) && Objects.equals(activities, that.activities) && Objects.equals(boundaries, that.boundaries) && Objects.equals(barriers, that.barriers) && Objects.equals(arcs, that.arcs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceFile, project, placement, resources, activities, boundaries, barriers, arcs);
    }
}
