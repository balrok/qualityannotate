package org.qualityannotate.coderepo.github;

import jakarta.enterprise.context.ApplicationScoped;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.qualityannotate.api.coderepository.CodeRepository;
import org.qualityannotate.api.coderepository.Comment;
import org.qualityannotate.api.coderepository.FileComment;

import java.io.IOException;
import java.util.List;

@ApplicationScoped
public class GithubCodeRepository implements CodeRepository {
    private final GithubConfig config;
    private final GitHubBuilder gitHubBuilder;

    public GithubCodeRepository(GithubConfig config) {
        this.config = config;
        gitHubBuilder = new GitHubBuilder().withOAuthToken("my_personal_token");
    }

    @Override
    public void createOrUpdateAnnotations(Comment globalComment, List<FileComment> fileComments) throws IOException {
        GitHub gh = gitHubBuilder.build();
        gh.checkApiUrlValidity();
        GHRepository repository = gh.getRepository(config.project());
        GHPullRequest pullRequest = repository.getPullRequest(Integer.parseInt(config.pullRequest()));
        pullRequest.comment(globalComment.markdown());
        for (FileComment fileComment : fileComments) {
            String markdown = fileComment.comment()
                                         .markdown();
            pullRequest.createReviewComment(markdown, config.commitHash(), fileComment.fileName(),
                    fileComment.linenumber());
        }
    }

    @Override
    public String printConfigWithoutSecrets() {
        return config.printWithoutSecrets();
    }
}
