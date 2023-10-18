package org.qualityannotate.core;

import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.qualityannotate.api.coderepository.CodeRepository;
import org.qualityannotate.api.qualitytool.MetricsAndIssues;
import org.qualityannotate.api.qualitytool.QualityTool;
import org.qualityannotate.coderepo.CodeRepoProvider;
import org.qualityannotate.coderepo.github.GithubConfig;
import org.qualityannotate.quality.QualityToolProvider;
import org.qualityannotate.quality.sonarqube.SonarqubeConfig;
import picocli.AutoComplete;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "qualityannotate", mixinStandardHelpOptions = true, subcommands = AutoComplete.GenerateCompletion.class,
         description = """
                 Helps extracting issues from quality tools like sonarqube and annotate them on your pull requests.
                 Make sure to have the application.yml located under config/application.yml
                 """)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class MainCommand implements Runnable {
    private final CodeRepoProvider codeRepoProvider;
    private final QualityToolProvider qualityToolProvider;
    @Parameters(paramLabel = "<qualitytool>", defaultValue = SonarqubeConfig.NAME,
                description = "From which quality-tool you want to retrieve the data. Possible Values: ${COMPLETION-CANDIDATES}; Default: ${DEFAULT-VALUE}",
                completionCandidates = QualityToolProvider.QualityToolCandidates.class)
    String qualityToolParam;
    @Parameters(paramLabel = "<coderepository>", defaultValue = GithubConfig.NAME,
                description = "To which code-repository you want to upload the data. Possible Values: ${COMPLETION-CANDIDATES}; Default: ${DEFAULT-VALUE}",
                completionCandidates = CodeRepoProvider.CodeRepositoryCandidates.class)
    String codeRepositoryParam;

    private static void updateAnnotations(QualityTool qualityTool, CodeRepository codeRepository,
                                          MetricsAndIssues metricsAndIssues) {
        try {
            codeRepository.createOrUpdateAnnotations(
                    CommentProcessor.createGlobalComment(metricsAndIssues.globalMetrics(), qualityTool),
                    CommentProcessor.createFileComments(metricsAndIssues.issues(), qualityTool));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static MetricsAndIssues getMetricsAndIssues(QualityTool qualityTool) {
        MetricsAndIssues metricsAndIssues;
        try {
            metricsAndIssues = qualityTool.getMetricsAndIssues();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return metricsAndIssues;
    }

    @Override
    public void run() {
        QualityTool qualityTool = qualityToolProvider.getQualityTool(qualityToolParam);
        CodeRepository codeRepository = codeRepoProvider.getCodeRepository(codeRepositoryParam);
        System.out.printf("Quality tool configured with\n%s\n", qualityTool.printConfigWithoutSecrets());
        System.out.printf("Code-Repository tool configured with\n%s\n", codeRepository.printConfigWithoutSecrets());
        MetricsAndIssues metricsAndIssues = getMetricsAndIssues(qualityTool);
        System.out.println(metricsAndIssues);
        updateAnnotations(qualityTool, codeRepository, metricsAndIssues);
    }

}
