package org.qualityannotate.coderepo;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import org.qualityannotate.api.coderepository.CodeRepository;
import org.qualityannotate.coderepo.github.GithubCodeRepository;
import org.qualityannotate.coderepo.github.GithubConfig;

@Singleton
@RequiredArgsConstructor(onConstructor_ = { @Inject })
public class CodeRepoProvider {
    private final GithubCodeRepository githubRepo;

    public CodeRepository getCodeRepository(String key) {
        switch (key) {
        case GithubConfig.NAME:
            return githubRepo;
        default:
            throw new IllegalArgumentException("Wrong code repository selected");
        }
    }

    public static class CodeRepositoryCandidates extends ArrayList<String> {
        CodeRepositoryCandidates() {
            super(List.of(GithubConfig.NAME));
        }
    }
}
