package org.qualityannotate.quality.sonarqube.client;

import lombok.Value;

import java.util.List;

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

    List<Period> periods;

    public Period getPeriod() {
        return periods.get(0);
    }

    /**
     * @param value     "value": "25",
     * @param bestValue "bestValue": false
     */
    public record Period(String value, Boolean bestValue) {
    }
}
