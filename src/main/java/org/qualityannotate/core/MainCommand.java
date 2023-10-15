package org.qualityannotate.core;

import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.qualityannotate.api.qualitytool.MetricsAndIssues;
import org.qualityannotate.quality.sonarqube.SonarqubeQualityTool;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "qualityannotate", mixinStandardHelpOptions = true)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class MainCommand implements Runnable {

    private final SonarqubeQualityTool sonarqubeApi;
    @Parameters(paramLabel = "<qualitytool>", defaultValue = "sonarqube",
            description = "From which quality-tool you want to retrieve the data")
    String qualityTool;

    @Override
    public void run() {
        System.out.printf("Using %s\n", qualityTool);
        MetricsAndIssues metricsAndIssues = sonarqubeApi.getMetricsAndIssues();
        System.out.println(metricsAndIssues);
    }

}
