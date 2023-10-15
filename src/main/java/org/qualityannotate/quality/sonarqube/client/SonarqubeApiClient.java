package org.qualityannotate.quality.sonarqube.client;


import jakarta.annotation.Nullable;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import java.util.Set;

@Path("/api")
public interface SonarqubeApiClient {
    /**
     * <a href="https://sonarqube.inria.fr/sonarqube/web_api/api/measures">docs</a>
     *
     * @param project    Component key. E.g. my_project
     * @param prId       Pull request id. Not available in the community edition. E.g. 1234
     * @param metricKeys Comma-separated list of additional fields that can be returned in the response.
     *                   E.g."new_coverage,new_sqale_debt_ratio,new_uncovered_conditions"
     */
    @GET()
    @Path("/measures/component")
    ComponentMeasures getComponentMeasures(@QueryParam("component") String project, @QueryParam("pullRequest") String prId, @QueryParam("metricsKeys") String metricKeys);


    /**
     * <a href="https://sonarqube.inria.fr/sonarqube/web_api/api/issues/search">docs</a>
     * TODO onComponentOnly might be interesting to retrieve global metrics
     *
     * @param project          Component key. E.g. my_project
     * @param prId             Pull request id. Not available in the community edition. E.g. 1234
     * @param branch           BBranch key. Not available in the community edition. E.g. feature/test
     * @param additionalFields Comma-separated list of the optional fields to be returned in response.
     * @param facets           Comma-separated list of the facets to be computed. No facet is computed by default.
     * @param pageNumber       Page number. E.g. 1
     * @param pageSize         Page size. Highest number is 500
     * @param resolved         To match resolved or unresolved issues
     * @param sort             Sort the issues
     * @param severities       Comma-separated list of severities
     * @param statuses         Comma-separated list of statuses
     */
    @GET()
    @Path("/issues/search")
    IssueSearch getIssuesSearch(@QueryParam("componentKeys") String project,
                                @Nullable @QueryParam("pullRequest") String prId,
                                @Nullable @QueryParam("branch") String branch,
                                @Nullable @QueryParam("additionalFields") @DefaultValue("_all") String additionalFields,
                                @Nullable @QueryParam("facets") @DefaultValue("severities,types") String facets,
                                @Nullable @QueryParam("p") @DefaultValue("1") String pageNumber,
                                // 500 is the biggest size
                                @Nullable @QueryParam("ps") @DefaultValue("500") String pageSize,
                                @Nullable @QueryParam("resolved") @DefaultValue("false") String resolved,
                                @Nullable @QueryParam("s") @DefaultValue("FILE_LINE") String sort,
                                // INFO got removed here
                                @Nullable @QueryParam("severities") @DefaultValue("MINOR,MAJOR,CRITICAL,BLOCKER") String severities,
                                @Nullable @QueryParam("statuses") @DefaultValue("OPEN,REOPENED,CONFIRMED") String statuses
    );
}
