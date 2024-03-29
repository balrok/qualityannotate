package org.qualityannotate.quality;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import org.qualityannotate.api.qualitytool.QualityTool;
import org.qualityannotate.coderepo.github.GithubConfig;
import org.qualityannotate.quality.sonarqube.SonarqubeConfig;
import org.qualityannotate.quality.sonarqube.SonarqubeQualityTool;

@Singleton
@RequiredArgsConstructor(onConstructor_ = { @Inject })
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
