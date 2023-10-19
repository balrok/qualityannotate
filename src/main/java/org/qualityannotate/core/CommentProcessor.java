package org.qualityannotate.core;

import java.util.List;
import java.util.stream.Collectors;

import org.qualityannotate.api.coderepository.Comment;
import org.qualityannotate.api.coderepository.FileComment;
import org.qualityannotate.api.qualitytool.GlobalMetrics;
import org.qualityannotate.api.qualitytool.Issue;
import org.qualityannotate.api.qualitytool.QualityTool;

public class CommentProcessor {
    private CommentProcessor() {
    }

    public static Comment createGlobalComment(GlobalMetrics globalMetrics, QualityTool qualityTool) {
        String markdown = String.format("""
                Code Quality Report for [SonarQube](%s)
                | Name | Value |
                |------|-------|
                %s
                """, qualityTool.getUrl(),
                globalMetrics.metrics()
                        .entrySet()
                        .stream()
                        .map(e -> "| " + e.getKey() + " | " + e.getValue() + " |")
                        .collect(Collectors.joining("\n")));
        return new Comment(markdown, markdown, "TODO-html");
    }

    public static List<FileComment> createFileComments(List<Issue> issues, QualityTool qualityTool) {
        return issues.stream().map(CommentProcessor::createComment).toList();
    }

    private static FileComment createComment(Issue issue) {
        String markdown = issue.comment() + (issue.urlToIssue() == null ? "" : "[issue](" + issue.urlToIssue() + ")");
        Comment comment = new Comment(markdown, markdown, "TODO-html");
        return new FileComment(issue.fileName(), issue.lineNumber(), comment);
    }
}
