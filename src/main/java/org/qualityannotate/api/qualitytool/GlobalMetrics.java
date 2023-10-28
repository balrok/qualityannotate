package org.qualityannotate.api.qualitytool;

import jakarta.annotation.Nullable;

import java.util.Map;
import java.util.Optional;

public record GlobalMetrics(Map<String, String> metrics, String url, @Nullable String statusUrl) {
    public Optional<String> getStatusUrl() {
        return Optional.ofNullable(statusUrl);
    }
}
