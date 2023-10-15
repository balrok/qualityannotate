package org.qualityannotate.quality.sonarqube.client;

import java.util.List;

public record IssueSearch(Paging paging, List<SqIssue> issues, List<Component> components, List<Facet> facets) {
}
