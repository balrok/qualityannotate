package org.qualityannotate.quality.sonarqube;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "sonarqube")
public interface SonarqubeConfig {
    @WithName("url")
    String url();

    @WithName("token")
    String token();
}
