package org.qualityannotate.quality.sonarqube;

import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.net.URI;
import java.util.*;

import org.qualityannotate.api.qualitytool.GlobalMetrics;
import org.qualityannotate.api.qualitytool.Issue;
import org.qualityannotate.api.qualitytool.MetricsAndIssues;
import org.qualityannotate.api.qualitytool.QualityTool;
import org.qualityannotate.core.rest.BasicAuthRequestFilter;
import org.qualityannotate.quality.sonarqube.client.*;

@ApplicationScoped
public class SonarqubeQualityTool implements QualityTool {
    private final SonarqubeConfig config;
    private final SonarqubeApiClient client;

    @Inject
    public SonarqubeQualityTool(SonarqubeConfig config) {
        this.config = config;
        client = QuarkusRestClientBuilder.newBuilder()
                .baseUri(URI.create(config.url()))
                .register(new BasicAuthRequestFilter(config.token()))
                .build(SonarqubeApiClient.class);
    }

    List<Issue> getIssues() {
        IssueSearch issuesSearch = client.getIssuesSearch(config.project(), config.pullRequest(), null);
        // TODO issueSearch.facets can be used for globalMetrics - but maybe doesn't matter
        List<Issue> issues = new ArrayList<>();
        for (SqIssue sqIssue : issuesSearch.issues()) {
            issues.add(new Issue(sqIssue.getPath(config.project()), sqIssue.textRange().startLine(), sqIssue.message(),
                    sqIssue.severity(), null));
        }
        return issues;
    }

    GlobalMetrics getGlobalMetrics() {
        ComponentMeasures componentMeasures = client.getComponentMeasures(config.project(), config.pullRequest(),
                String.join(",", config.globalMetricTypes()));
        Map<String, String> metrics = new HashMap<>();
        for (Metric metric : componentMeasures.getMetrics()) {
            Optional<Measure> measureOpt = componentMeasures.getComponent().getMeasure(metric.getKey());
            if (measureOpt.isPresent()) {
                Measure measure = measureOpt.get();
                String value = measure.getPeriod().map(Measure.Period::value).orElse(measure.getValue());
                if (value == null) {
                    value = "";
                }
                if (metric.getKey().endsWith("coverage")) {
                    try {
                        value = String.format("%.2f%%", Double.parseDouble(value));
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                } else {
                    try {
                        value = String.format("%.3f", Double.parseDouble(value));
                        value = value.replaceAll("\\.000$", "");
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
    public MetricsAndIssues getMetricsAndIssues() {
        return new MetricsAndIssues(getGlobalMetrics(), getIssues());
    }

    @Override
    public String getSeverityReadable(String severity) {
        return null;
    }

    @Override
    public String getSeverityUrl(String severity) {
        return null;
    }

    @Override
    public String printConfigWithoutSecrets() {
        return config.printWithoutSecrets();
    }

    /**
     * E.g. https://sonarcloud.io/summary/new_code?id=quyt_qualityannotate&pullRequest=1
     */
    @Override
    public String getUrl() {
        return getBaseUrl() + "/summary/new_code?id=" + config.project() + "&pullRequest=" + config.pullRequest();
    }

    private String getBaseUrl() {
        return config.url().trim().replaceAll("/$", "");
    }
}
