package org.qualityannotate.quality.sonarqube.client;


import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import java.util.Set;

@Path("/api")
public interface SonarqubeApiClient {
    /**
     * <a href="https://sonarqube.inria.fr/sonarqube/web_api/api/measures?internal=true">docs</a>
     *
     * @param project    Component key. E.g. my_project
     * @param prId       Pull request id. Not available in the community edition. E.g. 1234
     * @param metricKeys Comma-separated list of additional fields that can be returned in the response.
     *                   E.g."new_coverage,new_sqale_debt_ratio,new_uncovered_conditions"
     * @return
     */
    @GET()
    @Path("/measures/component")
    ComponentMeasures getComponentMeasures(@QueryParam("component") String project, @QueryParam("pullRequest") String prId, @QueryParam("metricsKeys") String metricKeys);
}
