package org.qualityannotate.quality.sonarqube.client;


import java.util.List;

/**
 * Single issue inside the project.
 *
 * @param key        unique identifier. E.g. 01fc972e-2a3c-433e-bcae-0bd7f88f5123
 * @param component  E.g. com.github.kevinsawicki:http-request:com.github.kevinsawicki.http.HttpRequest
 * @param project    E.g. com.github.kevinsawicki:http-request
 * @param rule       E.g. java:S1144
 * @param status     E.g. RESOLVED
 * @param resolution E.g. WONTFIX
 * @param severity   E.g. MAJOR
 *                   Deprecated: use {@param impacts}
 * @param message    E.g. Remove this unused private "getKee" method.
 * @param lineNumber Optional Line-number E.g. 81
 * @param hash       unique hash for this issue. E.g. a227e508d6646b55a086ee11d63b21e9
 * @param author     E.g. Developer 1
 * @param effort     E.g. 2h1min
 * @param type       E.g. CODE_SMELL
 *                   Deprecated: use {@param impacts}
 * @param textRange  E.g. startLine: 2, endline: 2, startOffset: 0, endOffset: 204
 */
public record SqIssue(String key, String component, String project, String rule, String status, String resolution,
                      @Deprecated String severity, String message, Integer lineNumber, String hash, String author,
                      String effort, @Deprecated String type,
                      org.qualityannotate.quality.sonarqube.client.SqIssue.TextRange textRange,
                      boolean quickFixAvailable, List<Impact> impacts) {
    /**
     * Computing the file-path requires to know the project-identifier.
     * It may not work if sonarqube was just using compiled files for analysis.
     *
     * @return File-path. E.g. src/main/Test.java
     */
    public String getPath(String project) {
        return component.replace(project, "").substring(1);
    }

    public record TextRange(int startLine, int endLine, int startOffset, int endOffset) {
    }

    /**
     * @param softwareQuality E.g. MAINTAINABILITY
     * @param severity        I.e. LOW, MEDIUM, HIGH
     */
    public record Impact(String softwareQuality, String severity) {
    }
}
