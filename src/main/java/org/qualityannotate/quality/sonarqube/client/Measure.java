package org.qualityannotate.quality.sonarqube.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Value;

@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class Measure {
    /**
     * "metric": "complexity",
     */
    String metric;

    /**
     * "value": "12"
     */
    String value;

    Period period;

    /**
     * @param value     "value": "25",
     * @param bestValue "bestValue": false
     */
    public record Period(String value, Boolean bestValue) {
    }
}
