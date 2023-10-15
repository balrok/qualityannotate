package org.qualityannotate.core;

import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.qualityannotate.api.coderepository.CodeRepository;
import org.qualityannotate.api.coderepository.Comment;
import org.qualityannotate.api.coderepository.FileComment;
import org.qualityannotate.api.qualitytool.GlobalMetrics;
import org.qualityannotate.api.qualitytool.Issue;
import org.qualityannotate.api.qualitytool.MetricsAndIssues;
import org.qualityannotate.api.qualitytool.QualityTool;
import org.qualityannotate.coderepo.github.GithubCodeRepository;
import org.qualityannotate.quality.sonarqube.SonarqubeQualityTool;
import picocli.AutoComplete;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Command(name = "qualityannotate", mixinStandardHelpOptions = true, subcommands = AutoComplete.GenerateCompletion.class,
         description = """
                 Helps extracting issues from quality tools like sonarqube and annotate them on your pull requests.
                 Make sure to have the application.yml located under config/application.yml
                 """)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class MainCommand implements Runnable {

    private final SonarqubeQualityTool sonarqubeApi;
    private final GithubCodeRepository githubApi;
    @Parameters(paramLabel = "<qualitytool>", defaultValue = "sonarqube",
                description = "From which quality-tool you want to retrieve the data. Possible Values: ${COMPLETION-CANDIDATES}; Default: ${DEFAULT-VALUE}",
                completionCandidates = QualityToolCandidates.class)
    String qualityToolParam;
    @Parameters(paramLabel = "<coderepository>", defaultValue = "github",
                description = "To which code-repository you want to upload the data. Possible Values: ${COMPLETION-CANDIDATES}; Default: ${DEFAULT-VALUE}",
                completionCandidates = CodeRepositoryCandidates.class)
    String codeRepositoryParam;

    @Override
    public void run() {
        QualityTool qualityTool = getQualityTool();
        CodeRepository codeRepository = getCodeRepository();
        System.out.printf("Quality tool configured with\n%s\n", qualityTool.printConfigWithoutSecrets());
        System.out.printf("Code-Repository tool configured with\n%s\n", codeRepository.printConfigWithoutSecrets());
        MetricsAndIssues metricsAndIssues = null;
        try {
            metricsAndIssues = qualityTool.getMetricsAndIssues();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println(metricsAndIssues);
        try {
            codeRepository.createOrUpdateAnnotations(createGlobalComment(metricsAndIssues.globalMetrics(), qualityTool),
                    createFileComments(metricsAndIssues.issues(), qualityTool));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Comment createGlobalComment(GlobalMetrics globalMetrics, QualityTool qualityTool) {
        String markdown = String.format("""
                Code Quality Report for [SonarQube](%s)
                | Name | Value |
                |------|-------|
                %s
                """, qualityTool.getUrl(), globalMetrics.metrics()
                                                        .entrySet()
                                                        .stream()
                                                        .map(e -> "| " + e.getKey() + " | " + e.getValue() + " |")
                                                        .collect(Collectors.joining("\n")));
        return new Comment("TODO-text", markdown, "TODO-html");
    }

    private List<FileComment> createFileComments(List<Issue> issues, QualityTool qualityTool) {
        return issues.stream()
                     .map(issue -> new FileComment(issue.fileName(), issue.lineNumber(),
                             new Comment("TODO-text",
                                     issue.comment() + (issue.urlToIssue() == null ? "" :
                                             "[issue](" + issue.urlToIssue() + ")"),
                                     "TODO-html")))
                     .toList();
    }

    private QualityTool getQualityTool() {
        switch (qualityToolParam) {
            case "sonarqube":
                return sonarqubeApi;
            default:
                throw new IllegalArgumentException("Wrong qualitytool selected");
        }
    }

    private CodeRepository getCodeRepository() {
        switch (codeRepositoryParam) {
            case "github":
                return githubApi;
            default:
                throw new IllegalArgumentException("Wrong code repository selected");
        }
    }

    public static class QualityToolCandidates extends ArrayList<String> {
        QualityToolCandidates() {
            super(List.of("sonarqube"));
        }
    }

    public static class CodeRepositoryCandidates extends ArrayList<String> {
        CodeRepositoryCandidates() {
            super(List.of("github"));
        }
    }
}
