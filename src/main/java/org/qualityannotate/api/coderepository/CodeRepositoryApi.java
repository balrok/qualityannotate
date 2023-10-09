package org.qualityannotate.api.coderepository;

import java.util.List;

public interface CodeRepositoryApi {
    void createOrUpdateGlobalAnnotation(Comment comment);

    void createOrUpdateFileAnnotations(List<FileComment> fileComments);
}
