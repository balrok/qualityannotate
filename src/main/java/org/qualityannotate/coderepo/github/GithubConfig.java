package org.qualityannotate.coderepo.github;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = GithubConfig.NAME)
public interface GithubConfig {
    String NAME = "github";

    @WithName("token")
    String token();

    @WithName("project")
    String project();

    @WithName("pull_request")
    Integer pullRequest();

    default String printWithoutSecrets() {
        return String.format("""
                project: %s
                pull_request: %s
                """, project(), pullRequest());
    }
}
