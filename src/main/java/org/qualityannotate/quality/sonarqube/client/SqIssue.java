package org.qualityannotate.quality.sonarqube.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Value;

/**
 * Single issue inside the project.
 * <p>
 * Following elements are not converted:
 * <pre>
 *      {
 *       "messageFormattings": [
 *         {
 *           "start": 0,
 *           "end": 4,
 *           "type": "CODE"
 *         }
 *       ],
 *       "creationDate": "2013-05-13T17:55:39+0200",
 *       "updateDate": "2013-05-13T17:55:39+0200",
 *       "tags": [
 *         "bug"
 *       ],
 *       "comments": [
 *         {
 *           "key": "7d7c56f5-7b5a-41b9-87f8-36fa70caa5ba",
 *           "login": "john.smith",
 *           "htmlText": "Must be &quot;public&quot;!",
 *           "markdown": "Must be \"public\"!",
 *           "updatable": false,
 *           "createdAt": "2013-05-13T18:08:34+0200"
 *         }
 *       ],
 *       "attr": {
 *         "jira-issue-key": "SONAR-1234"
 *       },
 *       "transitions": [
 *         "reopen"
 *       ],
 *       "actions": [
 *         "comment"
 *       ],
 *       "flows": [
 *         {
 *           "locations": [
 *             {
 *               "textRange": {
 *                 "startLine": 16,
 *                 "endLine": 16,
 *                 "startOffset": 0,
 *                 "endOffset": 30
 *               },
 *               "msg": "Expected position: 5",
 *               "msgFormattings": [
 *                 {
 *                   "start": 0,
 *                   "end": 4,
 *                   "type": "CODE"
 *                 }
 *               ]
 *             }
 *           ]
 *         },
 *         {
 *           "locations": [
 *             {
 *               "textRange": {
 *                 "startLine": 15,
 *                 "endLine": 15,
 *                 "startOffset": 0,
 *                 "endOffset": 37
 *               },
 *               "msg": "Expected position: 6",
 *               "msgFormattings": []
 *             }
 *           ]
 *         }
 *       ],
 *       "quickFixAvailable": false,
 *       "ruleDescriptionContextKey": "spring"
 *     }
 * </pre>
 */
@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class SqIssue {
    /**
     * unique identifier. E.g. 01fc972e-2a3c-433e-bcae-0bd7f88f5123
     */
    String key;

    /**
     * E.g. com.github.kevinsawicki:http-request:com.github.kevinsawicki.http.HttpRequest
     */
    String component;

    /**
     * E.g. com.github.kevinsawicki:http-request
     */
    String project;

    /**
     * E.g. java:S1144
     */
    String rule;

    /**
     * E.g. RESOLVED
     */
    String status;

    /**
     * E.g. WONTFIX
     */
    String resolution;

    /**
     * E.g. MAJOR
     */
    String severity;

    /**
     * E.g. Remove this unused private "getKee" method.
     */
    String message;

    /**
     * Optional Line-number E.g. 81
     */
    Integer lineNumber;

    /**
     * unique hash for this issue. E.g. a227e508d6646b55a086ee11d63b21e9
     */
    String hash;
    /**
     * E.g. Developer 1
     */
    String author;
    /**
     * E.g. 2h1min
     */
    String effort;
    /**
     * E.g. CODE_SMELL
     */
    String type;

    /**
     * E.g. startLine: 2, endline: 2, startOffset: 0, endOffset: 204
     */
    TextRange textRange;

    boolean quickFixAvailable;


    /**
     * Computing the file-path requires to know the project-identifier.
     * It may not work if sonarqube was just using compiled files for analysis.
     *
     * @return File-path. E.g. src/main/Test.java
     */
    public String getPath(String project) {
        return component.replace(project, "").substring(1);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)

    public record TextRange(int startLine, int endLine, int startOffset, int endOffset) {
    }
}
