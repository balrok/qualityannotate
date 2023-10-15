package org.qualityannotate.quality.sonarqube.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ComponentMeasures {
    @JsonProperty("component")
    public Component component;
    @JsonProperty("metrics")
    public List<Metric> metrics;
}
