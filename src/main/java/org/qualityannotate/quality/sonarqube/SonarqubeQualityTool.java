package org.qualityannotate.quality.sonarqube;

import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.qualityannotate.core.rest.BasicAuthRequestFilter;
import org.qualityannotate.quality.sonarqube.client.ComponentMeasures;
import org.qualityannotate.quality.sonarqube.client.Measure;
import org.qualityannotate.quality.sonarqube.client.Metric;
import org.qualityannotate.quality.sonarqube.client.SonarqubeApiClient;
import org.qualityannotate.api.qualitytool.GlobalMetrics;
import org.qualityannotate.api.qualitytool.Issue;
import org.qualityannotate.api.qualitytool.QualityTool;

import java.net.URI;
import java.util.*;

@ApplicationScoped
public class SonarqubeQualityTool implements QualityTool {
    private final SonarqubeConfig config;
    private final SonarqubeApiClient client;

    @Inject
    public SonarqubeQualityTool(SonarqubeConfig config) {
        this.config = config;
        client = QuarkusRestClientBuilder.newBuilder()
                .baseUri(URI.create(config.url()))
                .register(new BasicAuthRequestFilter("", config.token()))
                .build(SonarqubeApiClient.class);
    }

    @Override
    public List<Issue> getIssues() {
        return Collections.emptyList();
    }

    @Override
    public GlobalMetrics getGlobalMetrics() {
        ComponentMeasures componentMeasures = client.getComponentMeasures(config.project(), config.pullRequest(), "new_coverage,new_sqale_debt_ratio,new_uncovered_conditions");
        Map<String, String> metrics = new HashMap<>();
        for (Metric metric : componentMeasures.getMetrics()) {
            Optional<Measure> measure = componentMeasures.getComponent().getMeasure(metric.getKey());
            measure.ifPresent(value -> metrics.put(metric.getName(), value.getPeriod() == null ? value.getValue() : value.getPeriod().value()));
        }
        return new GlobalMetrics(metrics);
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