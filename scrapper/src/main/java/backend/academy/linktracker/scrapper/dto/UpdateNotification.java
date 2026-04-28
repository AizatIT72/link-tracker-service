package backend.academy.linktracker.scrapper.dto;

import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateNotification {
    private final String message;
    private final OffsetDateTime eventTime;
}
