package org.qualityannotate.quality;

import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.qualityannotate.api.qualitytool.QualityTool;
import org.qualityannotate.coderepo.github.GithubConfig;
import org.qualityannotate.quality.sonarqube.SonarqubeConfig;
import org.qualityannotate.quality.sonarqube.SonarqubeQualityTool;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class QualityToolProvider {
    private final SonarqubeQualityTool sonarqubeQualityTool;

    public QualityTool getQualityTool(String key) {
        switch (key) {
            case SonarqubeConfig.NAME:
                return sonarqubeQualityTool;
            default:
                throw new IllegalArgumentException("Wrong quality tool selected");
        }
    }

    public static class QualityToolCandidates extends ArrayList<String> {
        QualityToolCandidates() {
            super(List.of(GithubConfig.NAME));
        }
    }
}
