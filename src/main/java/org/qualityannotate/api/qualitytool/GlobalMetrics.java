package org.qualityannotate.api.qualitytool;

import java.util.Map;

public record GlobalMetrics(Map<String, String> metrics, String url) {
}
