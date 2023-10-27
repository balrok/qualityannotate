package org.qualityannotate.api.coderepository.api;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Abstraction for the API of Code repositories like Gitlab, github, bitbucket, etc.
 * Can be used best for APIs which are only text-based. So github-checks api and bitbucket-report api should use a
 * different interface.
 */
public interface CodeTextApi {
    Optional<CodeTextMainComment> getMainComment() throws IOException;

    void createMainComment(String comment) throws IOException;

    List<CodeTextFileComment> listFileComments() throws IOException;

    void createFileComment(String file, Integer line, String comment) throws IOException;
}
