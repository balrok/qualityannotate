package org.qualityannotate.quality.sonarqube.client;

import lombok.Value;

import java.util.List;
import java.util.Optional;

@Value
public class Component {
    /**
     * "key": "MY_PROJECT:ElementImpl.java",
     */
    String key;
    /**
     * "path": "src/main/java/com/sonarsource/markdown/impl/ElementImpl.java",
     */
    String path;

    List<Measure> measures;

    public Optional<Measure> getMeasure(String key) {
        return measures.stream().filter(measure -> measure.getMetric().equals(key)).findFirst();
    }

}
