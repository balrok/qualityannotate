package org.qualityannotate.quality.sonarqube.client;

import lombok.Value;

import java.util.List;
import java.util.Optional;

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

    List<Period> periods;

    public Optional<Period> getPeriod() {
        if (period != null) {
            return Optional.of(period);
        }
        if (periods != null && !periods.isEmpty()) {
            return Optional.of(periods.get(0));
        }
        return Optional.empty();
    }

    /**
     * @param value     "value": "25",
     * @param bestValue "bestValue": false
     */
    public record Period(String value, Boolean bestValue) {
    }
}
