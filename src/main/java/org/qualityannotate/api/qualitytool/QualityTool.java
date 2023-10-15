package org.qualityannotate.api.qualitytool;

import java.util.List;

public interface QualityTool {
    List<Issue> getIssues();

    GlobalMetrics getGlobalMetrics();

    String getSeverityReadable(String severity);

    String getSeverityUrl(String severity);
}