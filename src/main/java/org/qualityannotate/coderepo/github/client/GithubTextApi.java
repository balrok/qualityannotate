package org.qualityannotate.coderepo.github.client;

import io.quarkus.logging.Log;
import org.apache.commons.lang3.tuple.Pair;
import org.kohsuke.github.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.qualityannotate.api.coderepository.api.CodeTextApi;
import org.qualityannotate.api.coderepository.api.CodeTextFileComment;
import org.qualityannotate.api.coderepository.api.CodeTextMainComment;
import org.qualityannotate.coderepo.github.GithubConfig;

public class GithubTextApi implements CodeTextApi {
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

    public GithubTextApi(GithubConfig config) {
        gitHubBuilder = new GitHubBuilder().withOAuthToken(config.token());
        this.project = config.project();
        this.pullRequestId = config.pullRequest();
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
    public Optional<CodeTextMainComment> getMainComment() throws IOException {
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
        GHPullRequest pullRequest = getPR();
        System.out.println("Creating new global comment");
        pullRequest.comment(comment);
    }

    @Override
    public List<CodeTextFileComment> listFileComments() throws IOException {
        Log.info("Start creating file comments");
        GHPullRequest pullRequest = getPR();
        List<CodeTextFileComment> result = new ArrayList<>();
        for (GHPullRequestReviewComment review : pullRequest.listReviewComments()) {
            if (review.getUser().getId() == userId) {
                result.add(new CodeTextFileComment() {
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
        GHPullRequest pullRequest = getPR();
        String commitHash = pullRequest.getHead().getSha();
        line = (line == null || line < 1) ? 1 : line;
        pullRequest.createReviewComment(comment, commitHash, file, line);
    }
}
