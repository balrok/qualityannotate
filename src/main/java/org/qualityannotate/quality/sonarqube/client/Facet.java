package org.qualityannotate.quality.sonarqube.client;

import java.util.List;

public record Facet(List<FacetValue> values) {

    /**
     * @param val   human readable name of the Facet
     * @param count some metric count
     */
    public record FacetValue(String val, String count) {

    }
}
