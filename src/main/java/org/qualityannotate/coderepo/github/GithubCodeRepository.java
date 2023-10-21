package org.qualityannotate.coderepo.github;

import jakarta.enterprise.context.ApplicationScoped;

import org.qualityannotate.api.coderepository.AbstractCodeRepository;
import org.qualityannotate.api.coderepository.CodeRepository;
import org.qualityannotate.api.coderepository.api.CodeApi;
import org.qualityannotate.coderepo.github.client.GithubApi;

@ApplicationScoped
public class GithubCodeRepository extends AbstractCodeRepository implements CodeRepository {
    private final GithubConfig config;
    private final GithubApi githubApi;

    public GithubCodeRepository(GithubConfig config) {
        this.config = config;
        this.githubApi = new GithubApi(config.token(), config.project(), config.pullRequest());
    }

    @Override
    public String printConfigWithoutSecrets() {
        return config.printWithoutSecrets();
    }

    @Override
    protected CodeApi getCodeApi() {
        return githubApi;
    }
}
