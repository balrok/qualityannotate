package org.qualityannotate.quality.sonarqube.client;

import lombok.Value;

@Value
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
