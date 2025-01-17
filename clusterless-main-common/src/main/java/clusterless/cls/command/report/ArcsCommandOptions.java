/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.command.report;

import picocli.CommandLine;

import java.util.List;

public class ArcsCommandOptions extends ReportCommandOptions {
    @CommandLine.Mixin
    ArcReportOptions arcReportOptions = new ArcReportOptions();

    public ArcReportOptions setProjects(List<String> projects) {
        return arcReportOptions.setProjects(projects);
    }

    public ReportOptions setProfile(String profile) {
        return arcReportOptions.setProfile(profile);
    }

    public ReportOptions setAccount(String account) {
        return arcReportOptions.setAccount(account);
    }

    public ReportOptions setRegion(String region) {
        return arcReportOptions.setRegion(region);
    }

    public ReportOptions setStage(String stage) {
        return arcReportOptions.setStage(stage);
    }

    public List<String> projects() {
        return arcReportOptions.projects();
    }

    public String profile() {
        return arcReportOptions.profile();
    }

    public String account() {
        return arcReportOptions.account();
    }

    public String region() {
        return arcReportOptions.region();
    }

    public String stage() {
        return arcReportOptions.stage();
    }
}
