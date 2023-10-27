package org.qualityannotate.coderepo.github;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

import org.qualityannotate.api.coderepository.CodeRepository;
import org.qualityannotate.api.coderepository.CodeStructuredExecutor;
import org.qualityannotate.api.coderepository.Comment;
import org.qualityannotate.api.coderepository.FileComment;
import org.qualityannotate.api.qualitytool.MetricsAndIssues;
import org.qualityannotate.coderepo.github.client.GithubStructuredApi;
import org.qualityannotate.coderepo.github.client.GithubTextApi;

@ApplicationScoped
public class GithubCodeRepository implements CodeRepository {
    private final GithubConfig config;
    private final GithubTextApi githubTextsApi;
    private final GithubStructuredApi githubStructuredApi;

    public GithubCodeRepository(GithubConfig config) {
        this.config = config;
        this.githubTextsApi = new GithubTextApi(config);
        this.githubStructuredApi = new GithubStructuredApi(config);
    }

    @Override
    public void createOrUpdateAnnotations(Comment globalComment, List<FileComment> fileComments,
            MetricsAndIssues metricsAndIssues) throws Exception {
        CodeStructuredExecutor.run(githubStructuredApi, metricsAndIssues);
        // CodeTextExecutor.run(githubTextsApi, globalComment, fileComments);
    }

    @Override
    public String printConfigWithoutSecrets() {
        return config.printWithoutSecrets();
    }

}
