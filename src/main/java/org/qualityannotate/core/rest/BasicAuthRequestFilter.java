package org.qualityannotate.core.rest;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Base64;

/**
 * Makes it possible to use basic auth with {@link io.quarkus.rest.client.reactive.QuarkusRestClientBuilder}
 * Use it like this:
 * <pre>
 * QuarkusRestClientBuilder.newBuilder()
 *     .register(new BasicAuthRequestFilter("user", "pass"))
 * </pre>
 */
@Priority(Priorities.AUTHENTICATION)
@RequiredArgsConstructor
public class BasicAuthRequestFilter implements ClientRequestFilter {
    final String user;
    final String password;

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        requestContext.getHeaders().add(HttpHeaders.AUTHORIZATION, getAccessToken());
    }

    private String getAccessToken() {
        return "Basic " + Base64.getEncoder().encodeToString((user + ":" + password).getBytes());
    }
}
