package org.qualityannotate.quality.sonarqube;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

import java.util.List;

@ConfigMapping(prefix = "sonarqube")
public interface SonarqubeConfig {
    @WithName("url")
    String url();

    @WithName("token")
    String token();

    @WithName("project")
    String project();

    @WithName("pull_request")
    String pullRequest();

    @WithName("global_metric_types")
    List<String> globalMetricTypes();

    default String printWithoutSecrets() {
        return String.format("""
                url: %s
                project: %s
                pull_request: %s
                global_metric_types: [%s]
                """, url(), project(), pullRequest(), String.join(",", globalMetricTypes()));
    }
}
