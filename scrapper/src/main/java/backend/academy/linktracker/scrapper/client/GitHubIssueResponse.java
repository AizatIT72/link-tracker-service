package backend.academy.linktracker.scrapper.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GitHubIssueResponse(
    Long id,
    String title,
    String body,
    User user,
    @JsonProperty("created_at")
    OffsetDateTime createdAt,
    @JsonProperty("html_url")
    String htmlUrl
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record User(String login) {}
}
