package org.qualityannotate.quality.sonarqube;

import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.qualityannotate.core.QualityToolConfig;
import org.qualityannotate.quality.sonarqube.client.Extension;
import org.qualityannotate.quality.sonarqube.client.ExtensionsService;
import org.qualityannotate.api.qualitytool.GlobalMetrics;
import org.qualityannotate.api.qualitytool.Issue;
import org.qualityannotate.api.qualitytool.QualityToolApi;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor(onConstructor_ = {@Inject})
@ApplicationScoped
public class SonarqubeApiImpl implements QualityToolApi {
    private final SonarqubeConfig config;

    @Override
    public List<Issue> getIssues() {
        ExtensionsService client = QuarkusRestClientBuilder.newBuilder()
                .baseUri(URI.create("https://stage.code.quarkus.io/api"))
                .build(ExtensionsService.class);
        Set<Extension> test = client.getById("io.quarkus:quarkus-resteasy-reactive-jackson");
        System.out.println(test);
        return Collections.emptyList();
    }

    @Override
    public GlobalMetrics getGlobalMetrics() {
        return null;
    }

    @Override
    public String getSeverityReadable(String severity) {
        return null;
    }

    @Override
    public String getSeverityUrl(String severity) {
        return null;
    }
}