package org.qualityannotate.api.qualitytool;

public interface QualityTool {
    MetricsAndIssues getMetricsAndIssues();

    String getSeverityReadable(String severity);

    String getSeverityUrl(String severity);
}