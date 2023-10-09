package org.qualityannotate.core;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "qualitytool")
public interface QualityToolConfig {

    @WithName("url")
    String url();

    @WithName("username")
    String username();

    @WithName("password")
    String password();

    @WithName("token")
    String token();
}