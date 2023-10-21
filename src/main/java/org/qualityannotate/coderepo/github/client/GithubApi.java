package org.qualityannotate.coderepo.github.client;

import io.quarkus.logging.Log;
import org.apache.commons.lang3.tuple.Pair;
import org.kohsuke.github.GHIssueComment;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHPullRequestReviewComment;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.PagedIterator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.qualityannotate.api.coderepository.api.CodeApi;
import org.qualityannotate.api.coderepository.api.CodeFileComment;
import org.qualityannotate.api.coderepository.api.CodeMainComment;

public class GithubApi implements CodeApi {
    private final GitHubBuilder gitHubBuilder;
    private final String project;
    private final int pullRequestId;
    private GHPullRequest pullRequestCache;
    /**
     * Guaranteed to be not null by {@link #init()}
     */
    private GitHub github = null;
    /**
     * Current user's id. Guaranteed to be not null by {@link #init()}
     */
    private long userId = 0L;

    public GithubApi(String token, String project, int pullRequestId) {
        gitHubBuilder = new GitHubBuilder().withOAuthToken(token);
        this.project = project;
        this.pullRequestId = pullRequestId;
    }

    private void init() throws IOException {
        if (github == null) {
            github = gitHubBuilder.build();
            github.checkApiUrlValidity();
            userId = github.getMyself().getId();
        }
    }

    private GHPullRequest getPR() throws IOException {
        init();
        if (pullRequestCache == null) {
            GHRepository repository = github.getRepository(project);
            pullRequestCache = repository.getPullRequest(pullRequestId);
        }
        return pullRequestCache;
    }

    @Override
    public Optional<CodeMainComment> getMainComment() throws IOException {
        init();
        GHPullRequest pullRequest = getPR();
        PagedIterator<GHIssueComment> commentIterator = pullRequest.listComments().iterator();
        while (commentIterator.hasNext()) {
            GHIssueComment comment = commentIterator.next();
            if (comment.getUser().getId() == userId) {
                return Optional.of(cmt -> {
                    Log.info("Updating global comment");
                    comment.update(cmt);
                });
            }
        }
        return Optional.empty();
    }

    @Override
    public void createMainComment(String comment) throws IOException {
        init();
        GHPullRequest pullRequest = getPR();
        System.out.println("Creating new global comment");
        pullRequest.comment(comment);
    }

    @Override
    public List<CodeFileComment> listFileComments() throws IOException {
        init();
        Log.info("Start creating file comments");
        GHPullRequest pullRequest = getPR();
        List<CodeFileComment> result = new ArrayList<>();
        for (GHPullRequestReviewComment review : pullRequest.listReviewComments()) {
            if (review.getUser().getId() == userId) {
                result.add(new CodeFileComment() {
                    @Override
                    public void update(String comment) throws IOException {
                        review.update(comment);
                    }

                    @Override
                    public void delete() throws IOException {
                        review.delete();
                    }

                    @Override
                    public Pair<String, Integer> getFileLine() {
                        return Pair.of(review.getPath(), review.getLine());
                    }

                    @Override
                    public String getComment() {
                        return review.getBody();
                    }
                });
            }
        }
        return result;
    }

    @Override
    public void createFileComment(String file, Integer line, String comment) throws IOException {
        init();
        GHPullRequest pullRequest = getPR();
        String commitHash = pullRequest.getHead().getSha();
        line = (line == null || line < 1) ? 1 : line;
        pullRequest.createReviewComment(comment, commitHash, file, line);
    }
}
