package org.qualityannotate.quality.sonarqube.client;

import lombok.Data;
import lombok.Value;

@Value
public class Metric {
    /**
     * E.g. complexity
     */
    String key;

    /**
     * E.g. Complexity
     */
    String name;

    /**
     * E.g. Cyclomatic complexity
     */
    String description;
}
