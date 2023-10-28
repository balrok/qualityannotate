package org.qualityannotate.coderepo.github.client;

import jakarta.enterprise.context.ApplicationScoped;
import org.kohsuke.github.GHCheckRun;
import org.kohsuke.github.GHCheckRunBuilder;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.PagedIterator;

import java.io.IOException;
import java.sql.Date;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.qualityannotate.api.coderepository.api.CodeStructuredApi;
import org.qualityannotate.api.qualitytool.Issue;
import org.qualityannotate.api.qualitytool.MetricsAndIssues;
import org.qualityannotate.coderepo.github.GithubConfig;
import org.qualityannotate.core.CommentProcessor;

/**
 * This is using the Github <a href="https://docs.github.com/en/rest/checks">checks api</a>.
 * It seems like it can't be executed locally but only in a github-actions job.
 */
@ApplicationScoped
public class GithubStructuredApi implements CodeStructuredApi {
    public static final String NAME = "qualityannotate";

    private final GitHubBuilder gitHubBuilder;
    private final String project;
    private final int pullRequestId;

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

    @Override
    public void update(MetricsAndIssues metricsAndIssues) throws IOException {
        GitHub github = gitHubBuilder.build();
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
        checkRunBuilder.withCompletedAt(Date.from(Instant.now()));

        GHCheckRunBuilder.Output output = new GHCheckRunBuilder.Output("Result",
                CommentProcessor.createGlobalComment(metricsAndIssues.globalMetrics()).markdown());
        List<Issue> issues = metricsAndIssues.issues();
        for (Issue issue : issues) {
            GHCheckRunBuilder.Annotation annotation = new GHCheckRunBuilder.Annotation(issue.fileName(),
                    issue.startLine(), Optional.ofNullable(issue.endLine()).orElse(issue.startLine()),
                    mapSeverity(issue.severityEnum()), CommentProcessor.createComment(issue).comment().text());
            Optional.ofNullable(issue.startColumn()).ifPresent(annotation::withStartColumn);
            Optional.ofNullable(issue.endColumn()).ifPresent(annotation::withEndColumn);
            output.add(annotation);
        }
        metricsAndIssues.globalMetrics()
                .getStatusUrl()
                .ifPresent(u -> output.add(new GHCheckRunBuilder.Image("Status", u)));
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
