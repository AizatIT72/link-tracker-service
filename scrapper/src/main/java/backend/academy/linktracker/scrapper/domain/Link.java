package backend.academy.linktracker.scrapper.domain;

import java.time.OffsetDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Link {

    private Long id;
    private Long chatId;
    private String url;
    private List<String> tags;
    private List<String> filters;
    private OffsetDateTime lastCheckedAt;
    private OffsetDateTime lastUpdatedAt;
}
