package org.qualityannotate.api.qualitytool;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;

public record Issue(String fileName, Integer startLine, @Nullable Integer endLine, @Nullable Integer startColumn,
        @Nullable Integer endColumn, String comment, String severity, Severity severityEnum,
        @Nullable String urlToIssue) {

    public Integer lineNumber() {
        return startLine;
    }

    @AllArgsConstructor
    @Getter
    public enum Severity {
        LOW("ℹ\uFE0F"), MEDIUM("⚠\uFE0F"), HIGH("\uD83D\uDED1");

        private final String unicodeIcon;
    }
}
