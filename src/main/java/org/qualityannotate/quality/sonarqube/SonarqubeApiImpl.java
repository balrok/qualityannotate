package org.qualityannotate.quality.sonarqube;

import qualityannotate.api.qualitytool.GlobalMetrics;
import qualityannotate.api.qualitytool.Issue;
import qualityannotate.api.qualitytool.QualityToolApi;

import java.util.List;

public class SonarqubeApiImpl implements QualityToolApi {
    @Override
    public List<Issue> getIssues() {
        return null;
    }

    @Override
    public GlobalMetrics getGlobalMetrics() {
        return null;
    }

    @Override
    public String getSeverityReadable(String severity) {
        return null;
    }

    @Override
    public String getSeverityUrl(String severity) {
        return null;
    }
}
