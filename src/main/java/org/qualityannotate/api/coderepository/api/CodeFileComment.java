package org.qualityannotate.api.coderepository.api;

import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;

public interface CodeFileComment {
    void update(String comment) throws IOException;

    void delete() throws IOException;

    Pair<String, Integer> getFileLine();

    String getComment();
}
