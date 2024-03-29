package org.qualityannotate.core;

import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import picocli.AutoComplete;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.List;
import java.util.stream.Collectors;

import org.qualityannotate.api.coderepository.CodeRepository;
import org.qualityannotate.api.coderepository.Comment;
import org.qualityannotate.api.coderepository.FileComment;
import org.qualityannotate.api.qualitytool.MetricsAndIssues;
import org.qualityannotate.api.qualitytool.QualityTool;
import org.qualityannotate.coderepo.CodeRepoProvider;
import org.qualityannotate.coderepo.github.GithubConfig;
import org.qualityannotate.quality.QualityToolProvider;
import org.qualityannotate.quality.sonarqube.SonarqubeConfig;

@Command(name = "qualityannotate", mixinStandardHelpOptions = true, subcommands = AutoComplete.GenerateCompletion.class, description = """
        Helps extracting issues from quality tools like sonarqube and annotate them on your pull requests.
        Make sure to have the application.yml located under config/application.yml
        """)
@RequiredArgsConstructor(onConstructor_ = { @Inject })
public class MainCommand implements Runnable {
    private final CodeRepoProvider codeRepoProvider;
    private final QualityToolProvider qualityToolProvider;

    @Parameters(paramLabel = "<qualitytool>", defaultValue = SonarqubeConfig.NAME, description = "From which quality-tool you want to retrieve the data. Possible Values: ${COMPLETION-CANDIDATES}; Default: ${DEFAULT-VALUE}", completionCandidates = QualityToolProvider.QualityToolCandidates.class)
    String qualityToolParam;

    @Parameters(paramLabel = "<coderepository>", defaultValue = GithubConfig.NAME, description = "To which code-repository you want to upload the data. Possible Values: ${COMPLETION-CANDIDATES}; Default: ${DEFAULT-VALUE}", completionCandidates = CodeRepoProvider.CodeRepositoryCandidates.class)
    String codeRepositoryParam;

    private static void updateAnnotations(CodeRepository codeRepository, MetricsAndIssues metricsAndIssues) {
        Comment globalComment = CommentProcessor.createGlobalComment(metricsAndIssues.globalMetrics());
        List<FileComment> fileComments = CommentProcessor.createFileComments(metricsAndIssues.issues());
        Log.infof("Global comment:\n%s", globalComment.text());
        Log.infof("Issues:\n%s",
                fileComments.stream()
                        .map(c -> String.format("%s:%d\n%s", c.fileName(), c.linenumber(), c.comment().text()))
                        .collect(Collectors.joining("\n\n")));
        try {
            codeRepository.createOrUpdateAnnotations(globalComment, fileComments, metricsAndIssues);
        } catch (Exception e) {
            Log.error("Updating the code-repository failed", e);
            System.exit(1);
        }
    }

    private static MetricsAndIssues getMetricsAndIssues(QualityTool qualityTool) {
        MetricsAndIssues metricsAndIssues;
        try {
            metricsAndIssues = qualityTool.getMetricsAndIssues();
        } catch (Exception e) {
            Log.error("Could not extract metrics", e);
            System.exit(1);
            return null;
        }
        return metricsAndIssues;
    }

    @Override
    public void run() {
        QualityTool qualityTool = qualityToolProvider.getQualityTool(qualityToolParam);
        CodeRepository codeRepository = codeRepoProvider.getCodeRepository(codeRepositoryParam);
        Log.infof("Quality tool configured with\n%s", qualityTool.printConfigWithoutSecrets());
        Log.infof("Code-Repository tool configured with\n%s", codeRepository.printConfigWithoutSecrets());
        MetricsAndIssues metricsAndIssues = getMetricsAndIssues(qualityTool);
        Log.info(metricsAndIssues);
        updateAnnotations(codeRepository, metricsAndIssues);
    }
}
