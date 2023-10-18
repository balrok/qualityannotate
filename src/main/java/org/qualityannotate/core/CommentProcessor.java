package org.qualityannotate.core;

import org.qualityannotate.api.coderepository.Comment;
import org.qualityannotate.api.coderepository.FileComment;
import org.qualityannotate.api.qualitytool.GlobalMetrics;
import org.qualityannotate.api.qualitytool.Issue;
import org.qualityannotate.api.qualitytool.QualityTool;

import java.util.List;
import java.util.stream.Collectors;

public class CommentProcessor {
    private CommentProcessor() {
    }

    public static Comment createGlobalComment(GlobalMetrics globalMetrics, QualityTool qualityTool) {
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

    public static List<FileComment> createFileComments(List<Issue> issues, QualityTool qualityTool) {
        return issues.stream()
                     .map(issue -> new FileComment(issue.fileName(), issue.lineNumber(),
                             new Comment("TODO-text",
                                     issue.comment() + (issue.urlToIssue() == null ? "" :
                                             "[issue](" + issue.urlToIssue() + ")"),
                                     "TODO-html")))
                     .toList();
    }
}
