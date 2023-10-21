package org.qualityannotate.api.coderepository.api;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface CodeApi {
    Optional<CodeMainComment> getMainComment() throws IOException;

    void createMainComment(String comment) throws IOException;

    List<CodeFileComment> listFileComments() throws IOException;

    void createFileComment(String file, Integer line, String comment) throws IOException;
}
