package org.qualityannotate.api.coderepository;

import java.util.List;

import org.qualityannotate.api.qualitytool.MetricsAndIssues;

public interface CodeRepository {
    void createOrUpdateAnnotations(Comment globalComment, List<FileComment> fileComments,
            MetricsAndIssues metricsAndIssues) throws Exception;

    String printConfigWithoutSecrets();
}
