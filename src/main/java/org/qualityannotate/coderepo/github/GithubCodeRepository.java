package org.qualityannotate.coderepo.github;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.qualityannotate.api.coderepository.CodeRepository;
import org.qualityannotate.api.coderepository.Comment;
import org.qualityannotate.api.coderepository.FileComment;
import org.qualityannotate.coderepo.github.client.GithubApi;

@ApplicationScoped
public class GithubCodeRepository implements CodeRepository {
    private final GithubConfig config;
    private final GithubApi githubApi;

    public GithubCodeRepository(GithubConfig config) {
        this.config = config;
        githubApi = new GithubApi(config.token());
    }

    private static Map<Pair<String, Integer>, String> convertFileCommentsToMap(List<FileComment> fileComments) {
        Map<Pair<String, Integer>, List<Comment>> fileLineToCommentList = new HashMap<>();
        for (FileComment fileComment : fileComments) {
            List<Comment> comments = fileLineToCommentList
                    .computeIfAbsent(Pair.of(fileComment.fileName(), fileComment.linenumber()), k -> new ArrayList<>());
            comments.add(fileComment.comment());
        }
        return fileLineToCommentList.entrySet()
                .stream()
                .map(e -> Map.entry(e.getKey(), e.getValue().stream().reduce((c1, c2) -> c1).orElse(Comment.EMPTY)))
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().markdown()));
    }

    @Override
    public void createOrUpdateAnnotations(Comment globalComment, List<FileComment> fileComments) {
        Optional<GithubApi.GithubMainComment> mainComment = Optional.empty();
        try {
            mainComment = githubApi.getMainComment(config.project(), config.pullRequest());
        } catch (IOException e) {
            Log.warn("Could not retrieve main comment");
        }
        mainComment.ifPresentOrElse(c -> {
            try {
                c.update(globalComment.markdown());
            } catch (IOException e) {
                Log.warn("Could not update main comment", e);
            }
        }, () -> {
            try {
                githubApi.createMainComment(config.project(), config.pullRequest(), globalComment.markdown());
            } catch (IOException e) {
                Log.warn("Could not create main comment", e);
            }
        });

        Map<Pair<String, Integer>, String> fileLineToComment = convertFileCommentsToMap(fileComments);
        List<GithubApi.GithubFileComment> githubFileComments = Collections.emptyList();
        try {
            githubFileComments = githubApi.listFileComments(config.project(), config.pullRequest());
        } catch (IOException e) {
            Log.warn("Could not retrieve comments");
        }
        for (GithubApi.GithubFileComment review : githubFileComments) {
            String comment = fileLineToComment.get(review.getFileLine());
            Log.info("Found existing file comment");
            if (comment != null) {
                fileLineToComment.remove(review.getFileLine());
                if (!review.getComment().equals(comment)) {
                    Log.info(" -> updating");
                    try {
                        review.update(comment);
                    } catch (IOException e) {

                        Log.warn("Could not update comment", e);
                    }
                }
            } else {
                Log.infof(" -> deleting %s", review.getFileLine());
                try {
                    review.delete();
                } catch (IOException e) {
                    Log.warn("Could not delete comment", e);
                }
            }
        }

        for (Map.Entry<Pair<String, Integer>, String> fileLineCommentEntry : fileLineToComment.entrySet()) {
            try {
                Log.infof("Creating %s", fileLineCommentEntry.getKey());
                githubApi.createFileComment(config.project(), config.pullRequest(),
                        fileLineCommentEntry.getKey().getLeft(), fileLineCommentEntry.getKey().getRight(),
                        fileLineCommentEntry.getValue());
            } catch (IOException e) {
                Log.warn("Could not create comment", e);
            }
        }
    }

    @Override
    public String printConfigWithoutSecrets() {
        return config.printWithoutSecrets();
    }
}
