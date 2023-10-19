package org.qualityannotate.coderepo.github;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public void createOrUpdateAnnotations(Comment globalComment, List<FileComment> fileComments) throws IOException {
        githubApi.createOrUpdateMainComment(globalComment.markdown(), config.project(), config.pullRequest());
        Map<Pair<String, Integer>, String> fileLineToComment = convertFileCommentsToMap(fileComments);
        githubApi.createOrUpdateFileComments(fileLineToComment, config.project(), config.pullRequest(),
                config.commitHash());
    }

    @Override
    public String printConfigWithoutSecrets() {
        return config.printWithoutSecrets();
    }
}
