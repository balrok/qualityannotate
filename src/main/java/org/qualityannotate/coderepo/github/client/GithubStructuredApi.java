package org.qualityannotate.coderepo.github.client;

import org.kohsuke.github.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.qualityannotate.api.coderepository.api.CodeStructuredApi;
import org.qualityannotate.api.qualitytool.Issue;
import org.qualityannotate.api.qualitytool.MetricsAndIssues;
import org.qualityannotate.coderepo.github.GithubConfig;
import org.qualityannotate.core.CommentProcessor;

public class GithubStructuredApi implements CodeStructuredApi {
    public static final String NAME = "qualityannotate";
    private final GitHubBuilder gitHubBuilder;
    private final String project;
    private final int pullRequestId;
    /**
     * Guaranteed to be not null by {@link #init()}
     */
    private GitHub github = null;

    public GithubStructuredApi(GithubConfig config) {
        gitHubBuilder = new GitHubBuilder().withOAuthToken(config.token());
        this.project = config.project();
        this.pullRequestId = config.pullRequest();
    }

    private static Optional<GHCheckRun> findCheckRun(GHRepository repository, GHPullRequest pullRequest)
            throws IOException {
        PagedIterator<GHCheckRun> iterator = repository.getCheckRuns(pullRequest.getHead().getSha()).iterator();
        while (iterator.hasNext()) {
            GHCheckRun next = iterator.next();
            if (next.getName().equals(NAME)) {
                return Optional.of(next);
            }
        }
        return Optional.empty();
    }

    private void init() throws IOException {
        if (github == null) {
            github = gitHubBuilder.build();
            // github.checkApiUrlValidity();
        }
    }

    @Override
    public void update(MetricsAndIssues metricsAndIssues) throws IOException {
        init();
        GHRepository repository = github.getRepository(project);
        GHPullRequest pullRequest = repository.getPullRequest(pullRequestId);
        Optional<GHCheckRun> checkRunOpt = findCheckRun(repository, pullRequest);
        if (checkRunOpt.isPresent()) {
            GHCheckRun checkRun = checkRunOpt.get();
            GHCheckRunBuilder checkRunBuilder = checkRun.update();
            updateCheckRun(checkRunBuilder, metricsAndIssues);
        } else {
            GHCheckRunBuilder checkRunBuilder = repository.createCheckRun(NAME, pullRequest.getHead().getSha());
            updateCheckRun(checkRunBuilder, metricsAndIssues);
        }
    }

    private void updateCheckRun(GHCheckRunBuilder checkRunBuilder, MetricsAndIssues metricsAndIssues)
            throws IOException {
        checkRunBuilder.withDetailsURL(metricsAndIssues.globalMetrics().url());
        checkRunBuilder.withStatus(GHCheckRun.Status.COMPLETED);
        checkRunBuilder.withConclusion(GHCheckRun.Conclusion.NEUTRAL);
        GHCheckRunBuilder.Output output = new GHCheckRunBuilder.Output("Result",
                CommentProcessor.createGlobalComment(metricsAndIssues.globalMetrics()).markdown());
        List<Issue> issues = metricsAndIssues.issues();
        for (Issue issue : issues) {
            output.add(new GHCheckRunBuilder.Annotation(issue.fileName(), issue.lineNumber(),
                    mapSeverity(issue.severityEnum()), CommentProcessor.createComment(issue).comment().markdown()));
        }
        checkRunBuilder.add(output);
        checkRunBuilder.create();
    }

    private GHCheckRun.AnnotationLevel mapSeverity(Issue.Severity severity) {
        return switch (severity) {
        case LOW -> GHCheckRun.AnnotationLevel.NOTICE;
        case MEDIUM -> GHCheckRun.AnnotationLevel.WARNING;
        case HIGH -> GHCheckRun.AnnotationLevel.FAILURE;
        };
    }
}
