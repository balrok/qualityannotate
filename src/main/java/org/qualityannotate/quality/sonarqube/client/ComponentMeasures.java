package org.qualityannotate.quality.sonarqube.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ComponentMeasures {
    @JsonProperty("component")
    public Component component;
    @JsonProperty("metrics")
    public List<Metric> metrics;
}
