package backend.academy.linktracker.scrapper.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StackOverflowAnswerResponse(List<Item> items) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(
        @JsonProperty("answer_id")
        Long answerId,
        @JsonProperty("question_id")
        Long questionId,
        Owner owner,
        @JsonProperty("creation_date")
        Long creationDate,
        String body
    ) {
       public OffsetDateTime getCreatedAt() {
           return creationDate == null ? null : Instant.ofEpochSecond(creationDate).atOffset(ZoneOffset.UTC);
       }
    }

    public record Owner(@JsonProperty("display_name") String displayName) {
    }
}
