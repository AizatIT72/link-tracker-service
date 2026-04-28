package backend.academy.linktracker.bot.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Ответ scrapper API, описывающий одну ссылку.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkResponse {

    private Long id;
    private String url;
    private List<String> tags;
    private List<String> filters;
}
