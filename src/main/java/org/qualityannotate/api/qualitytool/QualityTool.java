package org.qualityannotate.api.qualitytool;

public interface QualityTool {
    MetricsAndIssues getMetricsAndIssues() throws Exception;

    String getSeverityReadable(String severity);

    String getSeverityUrl(String severity);

    String printConfigWithoutSecrets();

    String getUrl();
}