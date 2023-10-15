package org.qualityannotate.api.qualitytool;

import jakarta.annotation.Nullable;

public record Issue(String fileName, Integer lineNumber, String comment, String severity, @Nullable String urlToIssue) {
}
