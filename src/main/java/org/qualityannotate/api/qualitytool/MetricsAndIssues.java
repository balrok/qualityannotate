package org.qualityannotate.api.qualitytool;

import java.util.List;

public record MetricsAndIssues(GlobalMetrics globalMetrics, List<Issue> issues) {
}
