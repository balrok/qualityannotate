package org.qualityannotate.api.coderepository;

import java.util.List;

public interface CodeRepository {
    void createOrUpdateAnnotations(Comment globalComment, List<FileComment> fileComments) throws Exception;

    String printConfigWithoutSecrets();
}
