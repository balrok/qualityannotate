package org.qualityannotate.coderepo.github;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;

import java.util.List;

import org.qualityannotate.api.coderepository.CodeRepository;
import org.qualityannotate.api.coderepository.CodeStructuredExecutor;
import org.qualityannotate.api.coderepository.CodeTextExecutor;
import org.qualityannotate.api.coderepository.Comment;
import org.qualityannotate.api.coderepository.FileComment;
import org.qualityannotate.api.qualitytool.MetricsAndIssues;
import org.qualityannotate.coderepo.github.client.GithubStructuredApi;
import org.qualityannotate.coderepo.github.client.GithubTextApi;

@ApplicationScoped
@AllArgsConstructor
public class GithubCodeRepository implements CodeRepository {
    private final GithubConfig config;
    private final GithubTextApi githubTextApi;
    private final GithubStructuredApi githubStructuredApi;

    @Override
    public void createOrUpdateAnnotations(Comment globalComment, List<FileComment> fileComments,
            MetricsAndIssues metricsAndIssues) throws Exception {
        if (config.useChecks()) {
            Log.info("Using the checks api");
            CodeStructuredExecutor.run(githubStructuredApi, metricsAndIssues);
        } else {
            Log.info("Using the comment-based api");
            CodeTextExecutor.run(githubTextApi, globalComment, fileComments);
        }
    }

    @Override
    public String printConfigWithoutSecrets() {
        return config.printWithoutSecrets();
    }
}
