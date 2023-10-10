package org.qualityannotate.core;

import jakarta.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.qualityannotate.api.qualitytool.Issue;
import org.qualityannotate.quality.sonarqube.SonarqubeApiImpl;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.List;

@Command(name = "qualityannotate", mixinStandardHelpOptions = true)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class MainCommand implements Runnable {

    @Parameters(paramLabel = "<qualitytool>", defaultValue = "sonarqube",
            description = "From which quality-tool you want to retrieve the data")
    String qualityTool;

    private final SonarqubeApiImpl sonarqubeApi;

    @Override
    public void run() {
        System.out.printf("Using %s\n", qualityTool);
        List<Issue> issues = sonarqubeApi.getIssues();
        System.out.println(issues);
    }

}
