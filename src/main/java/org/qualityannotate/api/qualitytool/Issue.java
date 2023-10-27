package org.qualityannotate.api.qualitytool;

import jakarta.annotation.Nullable;

public record Issue(String fileName, Integer lineNumber, String comment, String severity, String severityIcon,
        Severity severityEnum, @Nullable String urlToIssue) {

    public enum Severity {
        LOW, MEDIUM, HIGH
    }
}
