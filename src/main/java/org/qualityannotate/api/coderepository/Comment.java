package org.qualityannotate.api.coderepository;

public record Comment(String text, String markdown, String html) {

    public static Comment EMPTY = new Comment("", "", "");

    /**
     * Creates a new comment where the current comment gets appended by {@param other}.
     */
    Comment append(Comment other) {
        return new Comment(text + "\n\n" + other.text, markdown + "\n\n" + other.markdown,
                html + "<br/><br/>" + other.html);
    }
}
