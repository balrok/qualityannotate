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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GithubApi {
    private final GitHubBuilder gitHubBuilder;
    private final Map<Integer, GHPullRequest> pullRequestCache = new HashMap<>();
    /**
     * Guaranteed to be not null by {@link #init()}
     */
    private GitHub github = null;
    /**
     * Current user's id. Guaranteed to be not null by {@link #init()}
     */
    private long userId = 0L;

    public GithubApi(String token) {
        gitHubBuilder = new GitHubBuilder().withOAuthToken(token);
    }

    private void init() throws IOException {
        if (github == null) {
            github = gitHubBuilder.build();
            github.checkApiUrlValidity();
            userId = github.getMyself().getId();
        }
    }

    private GHPullRequest getPR(String project, int pullRequestId) throws IOException {
        init();
        GHPullRequest ghPullRequest = pullRequestCache.get(pullRequestId);
        if (ghPullRequest != null) {
            return ghPullRequest;
        }
        GHRepository repository = github.getRepository(project);
        ghPullRequest = repository.getPullRequest(pullRequestId);
        pullRequestCache.put(pullRequestId, ghPullRequest);
        return ghPullRequest;
    }

    public Optional<GithubMainComment> getMainComment(String project, int pullRequestId) throws IOException {
        init();
        GHPullRequest pullRequest = getPR(project, pullRequestId);
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

    public void createMainComment(String project, int pullRequestId, String comment) throws IOException {
        init();
        GHPullRequest pullRequest = getPR(project, pullRequestId);
        System.out.println("Creating new global comment");
        pullRequest.comment(comment);
    }

    public List<GithubFileComment> listFileComments(String project, int pullRequestId) throws IOException {
        init();
        Log.info("Start creating file comments");
        GHPullRequest pullRequest = getPR(project, pullRequestId);
        List<GithubFileComment> result = new ArrayList<>();
        for (GHPullRequestReviewComment review : pullRequest.listReviewComments()) {
            if (review.getUser().getId() == userId) {
                result.add(new GithubFileComment() {
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

    public void createFileComment(String project, int pullRequestId, String file, Integer line, String comment)
            throws IOException {
        init();
        GHPullRequest pullRequest = getPR(project, pullRequestId);
        String commitHash = pullRequest.getHead().getSha();
        line = (line == null || line < 1) ? 1 : line;
        pullRequest.createReviewComment(comment, commitHash, file, line);
    }

    public interface GithubMainComment {
        void update(String comment) throws IOException;
    }

    public interface GithubFileComment {
        void update(String comment) throws IOException;

        void delete() throws IOException;

        Pair<String, Integer> getFileLine();

        String getComment();
    }
}
