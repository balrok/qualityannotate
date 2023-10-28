package org.qualityannotate.core;

import java.util.List;
import java.util.stream.Collectors;

import org.qualityannotate.api.coderepository.Comment;
import org.qualityannotate.api.coderepository.FileComment;
import org.qualityannotate.api.qualitytool.GlobalMetrics;
import org.qualityannotate.api.qualitytool.Issue;

public class CommentProcessor {
    private CommentProcessor() {
    }

    public static Comment createGlobalComment(GlobalMetrics globalMetrics) {
        String markdown = String.format("""
                Code Quality Report for [SonarQube](%s)
                | Name | Value |
                |------|-------|
                %s
                """, globalMetrics.url(),
                globalMetrics.metrics()
                        .entrySet()
                        .stream()
                        .map(e -> "| " + e.getKey() + " | " + e.getValue() + " |")
                        .collect(Collectors.joining("\n")));
        return new Comment(markdown, markdown, "TODO-html");
    }

    public static List<FileComment> createFileComments(List<Issue> issues) {
        return issues.stream().map(CommentProcessor::createComment).toList();
    }

    public static FileComment createComment(Issue issue) {
        String markdown = String.format("""
                %s%s%s
                """, issue.severityEnum().getUnicodeIcon(), issue.comment(),
                (issue.urlToIssue() == null ? "" : " [details](" + issue.urlToIssue() + ")"));
        String text = String.format("""
                %s%s%s
                """, issue.severityEnum().getUnicodeIcon(), issue.comment(),
                (issue.urlToIssue() == null ? "" : " " + issue.urlToIssue()));
        Comment comment = new Comment(text, markdown, "TODO-html");
        return new FileComment(issue.fileName(), issue.lineNumber(), comment);
    }
}
