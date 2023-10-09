package org.qualityannotate.api.coderepository;

public record FileComment(String fileName, Integer linenumber, Comment comment) {
}
