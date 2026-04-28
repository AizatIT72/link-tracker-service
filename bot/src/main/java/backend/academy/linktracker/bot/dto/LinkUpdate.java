package backend.academy.linktracker.bot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO для получения обновления от Scrapper (POST /updates).
 * Соответствует OpenAPI контракту bot-api.yaml.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkUpdate {

    @NotNull
    private Long id;

    @NotNull
    private String url;

    private String description;

    @NotNull
    @JsonProperty("tgChatIds")
    private List<Long> tgChatIds;
}
