package org.qualityannotate.api.coderepository.api;

import java.io.IOException;

import org.qualityannotate.api.qualitytool.MetricsAndIssues;

/**
 * Abstraction for the API of Code repositories like Gitlab, github, bitbucket, etc.
 * Can be used best for APIs which are structured and know about issues with severity. So github-checks
 * api and bitbucket-report api are well suited.
 */
public interface CodeStructuredApi {
    void update(MetricsAndIssues metricsAndIssues) throws IOException;
}
