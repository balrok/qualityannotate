package org.qualityannotate.quality.sonarqube;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.jknack.handlebars.internal.Files;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.qualityannotate.api.qualitytool.GlobalMetrics;
import org.qualityannotate.api.qualitytool.Issue;

@QuarkusTest
public class SonarqubeQualityToolTest {
    static final WireMockServer WM = new WireMockServer(new WireMockConfiguration().dynamicPort());

    @Inject
    SonarqubeQualityTool cut;

    @Inject
    SonarqubeConfig config;

    @BeforeAll
    static void beforeAll() {
        WM.start();
    }

    @AfterAll
    static void afterAll() {
        WM.stop();
    }

    @Test
    void testGetGlobalMetrics() {
        String content = getContent("measures/component/sonarqube_component.json");
        WM.stubFor(get(urlPathTemplate("/api/measures/component")).withBasicAuth(config.token(), "")
                .withQueryParam("component", equalTo(config.project()))
                .withQueryParam("pullRequest", equalTo(config.pullRequest()))
                .withQueryParam("metricKeys", equalTo("test-metrics-1,test-metrics-2"))
                .withQueryParam("additionalFields", equalTo("metrics"))
                .willReturn(okJson(content)));
        GlobalMetrics globalMetrics = cut.getGlobalMetrics();
        assertEquals(Map.of("Complexity", "12", "New issues", "25", "Lines of code", "114"), globalMetrics.metrics());
        assertEquals("http://localhost:42345/summary/new_code?id=Test_Project&pullRequest=Test_Pr".replaceAll(":[0-9]+",
                ":123"), globalMetrics.url().replaceAll(":[0-9]+", ":123"));
    }

    @Test
    void testGetIssues() {
        String content = getContent("issues/search/sonarqube_issues.json");
        WM.stubFor(get(urlPathTemplate("/api/issues/search")).withBasicAuth(config.token(), "")
                .withQueryParam("componentKeys", equalTo(config.project()))
                .withQueryParam("pullRequest", equalTo("Test_Pr"))
                .withQueryParam("additionalFields", equalTo("_all"))
                .withQueryParam("facets", equalTo("severities,types"))
                .withQueryParam("p", equalTo("1"))
                .withQueryParam("ps", equalTo("500"))
                .withQueryParam("resolved", equalTo("false"))
                .withQueryParam("s", equalTo("FILE_LINE"))
                .withQueryParam("severities", equalTo("MINOR,MAJOR,CRITICAL,BLOCKER"))
                .withQueryParam("statuses", equalTo("OPEN,REOPENED,CONFIRMED"))
                .willReturn(okJson(content)));
        assertEquals(
                List.of(new Issue("om.github.kevinsawicki:http-request:com.github.kevinsawicki.http.HttpRequest", 2,
                        "Remove this unused private \"getKee\" method.", "MAJOR", "MAJOR", Issue.Severity.HIGH,
                        "https://sonarcloud.io/organizations/quyt/rules?open=java:S1144&rule_key=java:S1144")),
                cut.getIssues());
    }

    private String getContent(String name) {
        try {
            return Files.read(Paths.get("src/test/resources/sonarqube/api/" + name).toFile(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Priority(1)
    @Alternative
    @ApplicationScoped
    public static class SonarqubeConfig implements org.qualityannotate.quality.sonarqube.SonarqubeConfig {

        @Override
        public String url() {
            return WM.baseUrl();
        }

        @Override
        public String token() {
            return "Test_Token";
        }

        @Override
        public String project() {
            return "Test_Project";
        }

        @Override
        public String pullRequest() {
            return "Test_Pr";
        }

        @Override
        public List<String> globalMetricTypes() {
            return List.of("test-metrics-1", "test-metrics-2");
        }
    }
}
