package org.qualityannotate.quality.sonarqube.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Value;

@Value
@JsonIgnoreProperties(ignoreUnknown = true)
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
