package org.qualityannotate.api.qualitytool;

public record Issue(String fileName, Integer lineNumber, String comment, String severity) {
}
