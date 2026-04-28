package backend.academy.linktracker.bot.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Ответ scrapper API со списком ссылок (GET /links).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListLinksResponse {

    private List<LinkResponse> links;
    private Integer size;
}
