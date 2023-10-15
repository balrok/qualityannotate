package org.qualityannotate.quality.sonarqube;

import com.github.jknack.handlebars.internal.Files;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.qualityannotate.api.qualitytool.GlobalMetrics;
import org.qualityannotate.api.qualitytool.Issue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@QuarkusTest
public class SonarqubeQualityToolTest {
    static final WireMockServer WM = new WireMockServer(new WireMockConfiguration().dynamicPort());

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
        WM.stubFor(get(urlPathTemplate("/api/measures/component"))
                .withBasicAuth("", config.token())
                .withQueryParam("component", equalTo(config.project()))
                .withQueryParam("pullRequest", equalTo(config.pullRequest()))
                .withQueryParam("metricsKeys", equalTo("test-metrics-1,test-metrics-2"))
                .willReturn(okJson(content)));
        assertEquals(new GlobalMetrics(Map.of("New issues", "25", "Lines of code", "114", "Complexity", "12")), cut.getGlobalMetrics());
    }

    @Test
    void testGetIssues() {
        String content = getContent("issues/search/sonarqube_issues.json");
        WM.stubFor(get(urlPathTemplate("/api/issues/search"))
                .withBasicAuth("", config.token())
                .withQueryParam("componentKeys", equalTo(config.project()))
                .willReturn(okJson(content)));
        assertEquals(List.of(new Issue("om.github.kevinsawicki:http-request:com.github.kevinsawicki.http.HttpRequest",
                        null, "Remove this unused private \"getKee\" method.", "MAJOR")),
                cut.getIssues());
    }

    private String getContent(String name) {
        try {
            return Files.read(Paths.get("src/test/resources/sonarqube/api/" + name).toFile(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
