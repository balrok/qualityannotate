package org.qualityannotate.api.qualitytool;

public record Issue(String fileName, String lineNumber, String comment, String severity) {
}
