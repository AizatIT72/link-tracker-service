package backend.academy.linktracker.scrapper.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StackOverflowCommentResponse(List<Item> items) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(
        @JsonProperty("comment_id")
        Long commentId,
        @JsonProperty("post_id")
        Long postId,
        Owner owner,
        @JsonProperty("creation_date")
        Long creationDate,
        @JsonProperty("body_markdown")
        String body
    ) {
        public OffsetDateTime getCreatedAt() {
            return creationDate == null ? null : Instant.ofEpochSecond(creationDate).atOffset(ZoneOffset.UTC);
        }
    }

    public record Owner(@JsonProperty("display_name") String displayName) {
    }
}
