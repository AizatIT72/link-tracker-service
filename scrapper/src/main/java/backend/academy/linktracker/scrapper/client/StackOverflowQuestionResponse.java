package backend.academy.linktracker.scrapper.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

public record StackOverflowQuestionResponse(
        @JsonProperty("items") List<Item> items) {
    public record Item(
            @JsonProperty("question_id") Long questionId,
            @JsonProperty("title") String title,
            @JsonProperty("last_activity_date") Long lastActivityDate) {
        public OffsetDateTime getLastActivityAsDateTime() {
            if (lastActivityDate == null) return null;
            return Instant.ofEpochSecond(lastActivityDate).atOffset(ZoneOffset.UTC);
        }
    }
}
