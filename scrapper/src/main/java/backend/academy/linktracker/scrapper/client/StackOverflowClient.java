package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.properties.StackoverflowProperties;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class StackOverflowClient {

    private static final Pattern SO_PATTERN = Pattern.compile("https?://stackoverflow\\.com/questions/(\\d+)(?:/.*)?$");

    private final RestClient restClient;
    private final StackoverflowProperties properties;

    public StackOverflowClient(StackoverflowProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.stackexchange.com/2.3")
                .build();
    }

    public Optional<StackOverflowQuestionResponse> fetchQuestion(String url) {
        Matcher matcher = SO_PATTERN.matcher(url);
        if (!matcher.matches()) {
            return Optional.empty();
        }

        String questionId = matcher.group(1);

        log.atDebug().addKeyValue("questionId", questionId).log("Запрос к StackOverflow API");

        try {
            StackOverflowQuestionResponse response = restClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/questions/{id}")
                            .queryParam("site", "stackoverflow")
                            .queryParam("key", properties.getKey())
                            .build(questionId))
                    .retrieve()
                    .body(StackOverflowQuestionResponse.class);
            return Optional.ofNullable(response);
        } catch (HttpClientErrorException e) {
            log.atWarn()
                    .addKeyValue("url", url)
                    .addKeyValue("status", e.getStatusCode().value())
                    .log("Ошибка запроса к StackOverflow API");
            return Optional.empty();
        } catch (Exception e) {
            log.atError().addKeyValue("url", url).setCause(e).log("Неожиданная ошибка при запросе к StackOverflow API");
            return Optional.empty();
        }
    }

    public Optional<StackOverflowAnswerResponse> fetchAnswers(String url) {
        Matcher matcher = SO_PATTERN.matcher(url);
        if (!matcher.matches()) {
            return Optional.empty();
        }

        String questionId = matcher.group(1);

        log.atDebug().addKeyValue("questionId", questionId).log("Запрос Answers к StackOverflow API");

        try {
            StackOverflowAnswerResponse response = restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                    .path("/questions/{id}/answers")
                    .queryParam("site", "stackoverflow")
                    .queryParam("filter", "withbody")
                    .queryParam("key", properties.getKey())
                    .build(questionId))
                .retrieve()
                .body(StackOverflowAnswerResponse.class);
            return Optional.ofNullable(response);
        } catch (HttpClientErrorException e) {
            log.atWarn()
                .addKeyValue("url", url)
                .addKeyValue("status", e.getStatusCode().value())
                .log("Ошибка запроса Answers к StackOverflow API");
            return Optional.empty();
        } catch (Exception e) {
            log.atError().addKeyValue("url", url).setCause(e).log("Неожиданная ошибка при запросе Answers");
            return Optional.empty();
        }
    }

    public Optional<StackOverflowCommentResponse> fetchComments(String url) {
        Matcher matcher = SO_PATTERN.matcher(url);
        if (!matcher.matches()) {
            return Optional.empty();
        }

        String questionId = matcher.group(1);

        log.atDebug().addKeyValue("questionId", questionId).log("Запрос Comments к StackOverflow API");

        try {
            StackOverflowCommentResponse response = restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                    .path("/questions/{id}/comments")
                    .queryParam("site", "stackoverflow")
                    .queryParam("filter", "withbody")
                    .queryParam("key", properties.getKey())
                    .build(questionId))
                .retrieve()
                .body(StackOverflowCommentResponse.class);
            return Optional.ofNullable(response);
        } catch (HttpClientErrorException e) {
            log.atWarn()
                .addKeyValue("url", url)
                .addKeyValue("status", e.getStatusCode().value())
                .log("Ошибка запроса Comments к StackOverflow API");
            return Optional.empty();
        } catch (Exception e) {
            log.atError().addKeyValue("url", url).setCause(e).log("Неожиданная ошибка при запросе Comments");
            return Optional.empty();
        }
    }

    public boolean supports(String url) {
        return SO_PATTERN.matcher(url).matches();
    }
}
