package org.qualityannotate.api.coderepository;

import org.qualityannotate.api.coderepository.api.CodeStructuredApi;
import org.qualityannotate.api.qualitytool.MetricsAndIssues;

public final class CodeStructuredExecutor {
    private CodeStructuredExecutor() {

    }

    public static void run(CodeStructuredApi codeStructuredApi, MetricsAndIssues metricsAndIssues) throws Exception {
        codeStructuredApi.update(metricsAndIssues);
    }
}
