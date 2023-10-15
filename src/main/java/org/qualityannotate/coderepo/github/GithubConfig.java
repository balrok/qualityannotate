package org.qualityannotate.coderepo.github;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "github")
public interface GithubConfig {
    @WithName("token")
    String token();

    @WithName("project")
    String project();

    @WithName("pull_request")
    String pullRequest();

    @WithName("commit_hash")
    String commitHash();

    default String printWithoutSecrets() {
        return String.format("""
                project: %s
                pull_request: %s
                commit_hash: %s
                """, project(), pullRequest(), commitHash());
    }
}
