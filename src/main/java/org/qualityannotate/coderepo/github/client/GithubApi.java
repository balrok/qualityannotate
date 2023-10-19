package org.qualityannotate.coderepo.github.client;

import io.quarkus.logging.Log;
import org.apache.commons.lang3.tuple.Pair;
import org.kohsuke.github.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

    public void createOrUpdateMainComment(String globalComment, String project, int pullRequestId) throws IOException {
        init();
        GHPullRequest pullRequest = getPR(project, pullRequestId);
        PagedIterator<GHIssueComment> commentIterator = pullRequest.listComments().iterator();
        while (commentIterator.hasNext()) {
            GHIssueComment comment = commentIterator.next();
            if (comment.getUser().getId() == userId) {
                Log.info("Found comment: updating now");
                comment.update(globalComment);
                return;
            }
        }
        System.out.println("Found comment: updating now");
        pullRequest.comment(globalComment);
    }

    public void createOrUpdateFileComments(Map<Pair<String, Integer>, String> fileLineToComment, String project,
            int pullRequestId, String commitHash) throws IOException {
        init();
        GHPullRequest pullRequest = getPR(project, pullRequestId);
        for (GHPullRequestReviewComment review : pullRequest.listReviewComments()) {
            if (review.getUser().getId() == userId) {
                Pair<String, Integer> fileLine = Pair.of(review.getPath(), review.getLine());
                String comment = fileLineToComment.get(fileLine);
                if (comment != null) {
                    fileLineToComment.remove(fileLine);
                    review.update(comment);
                } else {
                    review.delete();
                }
            }
        }
        for (Map.Entry<Pair<String, Integer>, String> fileLineCommentEntry : fileLineToComment.entrySet()) {
            Integer lineNumber = fileLineCommentEntry.getKey().getRight();
            lineNumber = (lineNumber == null) ? 1 : lineNumber;
            String fileName = fileLineCommentEntry.getKey().getLeft();
            try {
                pullRequest.createReviewComment(fileLineCommentEntry.getValue(), commitHash, fileName, lineNumber);
            } catch (RuntimeException e) {
                Log.warnf("Could not annotate issue on %s:%d", fileName, lineNumber);
            }
        }
    }
}
