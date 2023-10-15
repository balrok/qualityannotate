package org.qualityannotate.quality.sonarqube.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

public record Facet(List<FacetValue> values) {

    /**
     * @param val   human readable name of the Facet
     * @param count some metric count
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FacetValue(String val, String count) {

    }
}
