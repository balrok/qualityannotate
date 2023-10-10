package org.qualityannotate.quality.sonarqube.client;


import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import java.util.Set;

@Path("/extensions")
public interface ExtensionsService {

    @GET
    Set<Extension> getById(@QueryParam("id") String id);
}
