package org.qualityannotate.api.coderepository;

import io.quarkus.logging.Log;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.qualityannotate.api.coderepository.api.CodeStructuredApi;
import org.qualityannotate.api.coderepository.api.CodeTextApi;
import org.qualityannotate.api.coderepository.api.CodeTextFileComment;
import org.qualityannotate.api.coderepository.api.CodeTextMainComment;
import org.qualityannotate.api.qualitytool.MetricsAndIssues;

public final class CodeTextExecutor {
    private CodeTextExecutor() {

    }

    public static void run(CodeTextApi codeTextApi, Comment globalComment, List<FileComment> fileComments)
            throws Exception {
        Optional<CodeTextMainComment> mainComment = Optional.empty();
        try {
            mainComment = codeTextApi.getMainComment();
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
                codeTextApi.createMainComment(globalComment.markdown());
            } catch (IOException e) {
                Log.warn("Could not create main comment", e);
            }
        });

        Map<Pair<String, Integer>, String> fileLineToComment = convertFileCommentsToMap(fileComments);
        List<CodeTextFileComment> githubFileComments = Collections.emptyList();
        try {
            githubFileComments = codeTextApi.listFileComments();
        } catch (IOException e) {
            Log.warn("Could not retrieve comments");
        }
        for (CodeTextFileComment review : githubFileComments) {
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
                codeTextApi.createFileComment(fileLineCommentEntry.getKey().getLeft(),
                        fileLineCommentEntry.getKey().getRight(), fileLineCommentEntry.getValue());
            } catch (IOException e) {
                Log.warn("Could not create comment", e);
            }
        }
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

    private static void runCodeStructuredApi(CodeStructuredApi codeStructuredApi, MetricsAndIssues metricsAndIssues)
            throws Exception {
        codeStructuredApi.update(metricsAndIssues);
    }
}
