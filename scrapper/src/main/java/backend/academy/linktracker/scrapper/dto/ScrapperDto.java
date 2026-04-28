package backend.academy.linktracker.scrapper.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public final class ScrapperDto {

    private ScrapperDto() {}

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddLinkRequest {
        private String link;
        private List<String> tags;
        private List<String> filters;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RemoveLinkRequest {
        private String link;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LinkResponse {
        private Long id;
        private String url;
        private List<String> tags;
        private List<String> filters;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListLinksResponse {
        private List<LinkResponse> links;
        private Integer size;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiErrorResponse {
        private String description;
        private String code;
        private String exceptionName;
        private String exceptionMessage;
        private List<String> stacktrace;
    }
}
