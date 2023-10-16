package org.qualityannotate.coderepo.github;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.tuple.Pair;
import org.kohsuke.github.*;
import org.qualityannotate.api.coderepository.CodeRepository;
import org.qualityannotate.api.coderepository.Comment;
import org.qualityannotate.api.coderepository.FileComment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class GithubCodeRepository implements CodeRepository {
    private final GithubConfig config;
    private final GitHubBuilder gitHubBuilder;

    public GithubCodeRepository(GithubConfig config) {
        this.config = config;
        gitHubBuilder = new GitHubBuilder().withOAuthToken(config.token());
    }

    private static void createOrUpdateMainComment(Comment globalComment, long userId,
                                                  GHPullRequest pullRequest) throws IOException {
        PagedIterator<GHIssueComment> commentIterator = pullRequest.listComments().iterator();
        while (commentIterator.hasNext()) {
            GHIssueComment comment = commentIterator.next();
            if (comment.getUser().getId() == userId) {
                System.out.println("Found comment: updating now");
                comment.update(globalComment.markdown());
                return;
            }
        }
        System.out.println("Found comment: updating now");
        pullRequest.comment(globalComment.markdown());
    }

    private static Map<Pair<String, Integer>, Comment> convertFileCommentsToMap(List<FileComment> fileComments) {
        Map<Pair<String, Integer>, List<Comment>> fileLineToCommentList = new HashMap<>();
        for (FileComment fileComment : fileComments) {
            List<Comment> comments =
                    fileLineToCommentList.computeIfAbsent(Pair.of(fileComment.fileName(), fileComment.linenumber()),
                            k -> new ArrayList<>());
            comments.add(fileComment.comment());
        }
        Map<Pair<String, Integer>, Comment> fileLineToComment =
                fileLineToCommentList.entrySet().stream().map(e -> Map.entry(e.getKey(),
                                             e.getValue().stream().reduce((c1, c2) -> c1).orElse(Comment.EMPTY)))
                                     .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return fileLineToComment;
    }

    @Override
    public void createOrUpdateAnnotations(Comment globalComment, List<FileComment> fileComments) throws IOException {
        GitHub gh = gitHubBuilder.build();
        gh.checkApiUrlValidity();
        long userId = gh.getMyself().getId();
        GHRepository repository = gh.getRepository(config.project());
        GHPullRequest pullRequest = repository.getPullRequest(Integer.parseInt(config.pullRequest()));

        createOrUpdateMainComment(globalComment, userId, pullRequest);
        createOrUpdateFileComments(fileComments, userId, pullRequest);
    }

    private void createOrUpdateFileComments(List<FileComment> fileComments, long userId,
                                            GHPullRequest pullRequest) throws IOException {
        Map<Pair<String, Integer>, Comment> fileLineToComment = convertFileCommentsToMap(fileComments);
        for (GHPullRequestReviewComment review : pullRequest.listReviewComments()) {
            if (review.getUser().getId() == userId) {
                Pair<String, Integer> fileLine = Pair.of(review.getPath(), review.getLine());
                Comment comment = fileLineToComment.get(fileLine);
                if (comment != null) {
                    fileLineToComment.remove(fileLine);
                    review.update(comment.markdown());
                } else {
                    review.delete();
                }
            }
        }
        for (Map.Entry<Pair<String, Integer>, Comment> fileLineCommentEntry : fileLineToComment.entrySet()) {
            pullRequest.createReviewComment(fileLineCommentEntry.getValue().markdown(), config.commitHash(),
                    fileLineCommentEntry.getKey().getLeft(),
                    fileLineCommentEntry.getKey().getRight());
        }
    }

    @Override
    public String printConfigWithoutSecrets() {
        return config.printWithoutSecrets();
    }
}
