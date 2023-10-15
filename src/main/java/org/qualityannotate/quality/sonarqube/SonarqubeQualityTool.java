package org.qualityannotate.quality.sonarqube;

import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.qualityannotate.core.rest.BasicAuthRequestFilter;
import org.qualityannotate.quality.sonarqube.client.*;
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
        IssueSearch issuesSearch = client.getIssuesSearch(config.project(), config.pullRequest(), null, null, null, null, null, null, null, null, null);
        // TODO issueSearch.facets can be used for globalMetrics - but maybe doesn't matter
        List<Issue> issues = new ArrayList<>();
        for (SqIssue sqIssue : issuesSearch.issues()) {
            issues.add(new Issue(sqIssue.getPath(config.project()), sqIssue.getLineNumber(), sqIssue.getMessage(), sqIssue.getSeverity()));
        }
        return issues;
    }

    @Override
    public GlobalMetrics getGlobalMetrics() {
        ComponentMeasures componentMeasures = client.getComponentMeasures(config.project(), config.pullRequest(),
                String.join(",", config.globalMetricTypes()));
        Map<String, String> metrics = new HashMap<>();
        for (Metric metric : componentMeasures.getMetrics()) {
            Optional<Measure> measureOpt = componentMeasures.getComponent().getMeasure(metric.getKey());
            if (measureOpt.isPresent()) {
                Measure measure = measureOpt.get();
                String value = measure.getPeriod() == null ? measure.getValue() : measure.getPeriod().value();
                if (metric.getKey().endsWith("coverage")) {
                    try {
                        value = String.format("%.2f%%", Double.parseDouble(value));
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                }
                metrics.put(metric.getName(), value);
            }
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