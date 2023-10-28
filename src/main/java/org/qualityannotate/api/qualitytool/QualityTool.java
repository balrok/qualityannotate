package org.qualityannotate.api.qualitytool;

public interface QualityTool {
    MetricsAndIssues getMetricsAndIssues() throws Exception;

    String printConfigWithoutSecrets();
}
