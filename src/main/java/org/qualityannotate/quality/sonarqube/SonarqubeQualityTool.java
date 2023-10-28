package org.qualityannotate.quality.sonarqube;

import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.qualityannotate.api.qualitytool.GlobalMetrics;
import org.qualityannotate.api.qualitytool.Issue;
import org.qualityannotate.api.qualitytool.MetricsAndIssues;
import org.qualityannotate.api.qualitytool.QualityTool;
import org.qualityannotate.core.rest.BasicAuthRequestFilter;
import org.qualityannotate.quality.sonarqube.client.ComponentMeasures;
import org.qualityannotate.quality.sonarqube.client.IssueSearch;
import org.qualityannotate.quality.sonarqube.client.Measure;
import org.qualityannotate.quality.sonarqube.client.Metric;
import org.qualityannotate.quality.sonarqube.client.SonarqubeApiClient;
import org.qualityannotate.quality.sonarqube.client.SqIssue;

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
            issues.add(new Issue(sqIssue.getPath(config.project()), sqIssue.textRange().startLine(),
                    sqIssue.textRange().endLine(), sqIssue.textRange().startOffset(), sqIssue.textRange().startOffset(),
                    sqIssue.getQualityType().map(q -> q + ": ").orElse("") + sqIssue.message(), sqIssue.getSeverity(),
                    getSeverityEnum(sqIssue.getSeverity()), getIssueUrl(sqIssue.rule())));
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
        return new GlobalMetrics(metrics, getUrl(), getStatusUrl());
    }

    private String getIssueUrl(String rule) {
        if (rule == null) {
            return null;
        }
        return String.format("https://sonarcloud.io/organizations/quyt/rules?open=%s&rule_key=%s", rule, rule);
        // TODO link to the config.url or find the right organization on sonarcloud
        // return config.url() + String.format("coding_rules?open=%s&rule_key=%s", rule, rule);
    }

    @Override
    public MetricsAndIssues getMetricsAndIssues() {
        return new MetricsAndIssues(getGlobalMetrics(), getIssues());
    }

    public Issue.Severity getSeverityEnum(String severity) {
        return switch (severity.toLowerCase(Locale.ENGLISH)) {
        case "low" -> Issue.Severity.LOW;
        case "medium" -> Issue.Severity.MEDIUM;
        // case "high" -> Issue.Severity.HIGH;
        default -> Issue.Severity.HIGH;
        };
    }

    @Override
    public String printConfigWithoutSecrets() {
        return config.printWithoutSecrets();
    }

    /**
     * E.g. <a href="https://sonarcloud.io/summary/new_code?id=quyt_qualityannotate&pullRequest=1">example</a>
     */
    public String getUrl() {
        return getBaseUrl() + "/summary/new_code?id=" + config.project() + "&pullRequest=" + config.pullRequest();
    }

    /**
     * E.g.
     * <a href="https://sonarcloud.io/api/project_badges/measure?project=quyt_qualityannotate&pullRequest=4&metric=alert_status">example</a>
     */
    public String getStatusUrl() {
        return getBaseUrl() + "/api/project_badges/measure?project=" + config.project() + "&pullRequest="
                + config.pullRequest() + "&metric=alert_status";
    }

    private String getBaseUrl() {
        return config.url().trim().replaceAll("/$", "");
    }
}
