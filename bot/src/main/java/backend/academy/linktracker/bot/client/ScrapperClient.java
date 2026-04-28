package backend.academy.linktracker.bot.client;

import backend.academy.linktracker.bot.dto.AddLinkRequest;
import backend.academy.linktracker.bot.dto.LinkResponse;
import backend.academy.linktracker.bot.dto.ListLinksResponse;
import backend.academy.linktracker.bot.dto.RemoveLinkRequest;
import backend.academy.linktracker.bot.exception.ScrapperClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

/**
 * HTTP-клиент для взаимодействия с сервисом Scrapper.
 * Использует RestClient.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ScrapperClient {

    private static final String TG_CHAT_HEADER = "Tg-Chat-Id";

    private final RestClient scrapperRestClient;

    /**
     * Зарегистрировать Telegram-чат (POST /tg-chat/{id}).
     */
    public void registerChat(long chatId) {
        log.atInfo().addKeyValue("chatId", chatId).log("Регистрация чата в scrapper");
        try {
            scrapperRestClient.post().uri("/tg-chat/{id}", chatId).retrieve().toBodilessEntity();
        } catch (HttpClientErrorException e) {
            log.atError()
                    .addKeyValue("chatId", chatId)
                    .addKeyValue("status", e.getStatusCode().value())
                    .log("Ошибка регистрации чата");
            throw new ScrapperClientException("Ошибка регистрации чата: " + e.getMessage(), e);
        }
    }

    /**
     * Удалить Telegram-чат (DELETE /tg-chat/{id}).
     */
    public void deleteChat(long chatId) {
        log.atInfo().addKeyValue("chatId", chatId).log("Удаление чата в scrapper");
        try {
            scrapperRestClient.delete().uri("/tg-chat/{id}", chatId).retrieve().toBodilessEntity();
        } catch (HttpClientErrorException e) {
            log.atError()
                    .addKeyValue("chatId", chatId)
                    .addKeyValue("status", e.getStatusCode().value())
                    .log("Ошибка удаления чата");
            throw new ScrapperClientException("Ошибка удаления чата: " + e.getMessage(), e);
        }
    }

    /**
     * Получить список отслеживаемых ссылок (GET /links).
     */
    public ListLinksResponse getLinks(long chatId) {
        log.atInfo().addKeyValue("chatId", chatId).log("Получение списка ссылок из scrapper");
        try {
            return scrapperRestClient
                    .get()
                    .uri("/links")
                    .header(TG_CHAT_HEADER, String.valueOf(chatId))
                    .retrieve()
                    .body(ListLinksResponse.class);
        } catch (HttpClientErrorException e) {
            log.atError()
                    .addKeyValue("chatId", chatId)
                    .addKeyValue("status", e.getStatusCode().value())
                    .log("Ошибка получения ссылок");
            throw new ScrapperClientException("Ошибка получения ссылок: " + e.getMessage(), e);
        }
    }

    /**
     * Добавить ссылку в отслеживание (POST /links).
     */
    public LinkResponse addLink(long chatId, AddLinkRequest request) {
        log.atInfo()
                .addKeyValue("chatId", chatId)
                .addKeyValue("url", request.getLink())
                .log("Добавление ссылки в scrapper");
        try {
            return scrapperRestClient
                    .post()
                    .uri("/links")
                    .header(TG_CHAT_HEADER, String.valueOf(chatId))
                    .body(request)
                    .retrieve()
                    .body(LinkResponse.class);
        } catch (HttpClientErrorException e) {
            log.atError()
                    .addKeyValue("chatId", chatId)
                    .addKeyValue("url", request.getLink())
                    .addKeyValue("status", e.getStatusCode().value())
                    .log("Ошибка добавления ссылки");
            throw new ScrapperClientException("Ошибка добавления ссылки: " + e.getMessage(), e);
        }
    }

    /**
     * Удалить ссылку из отслеживания (DELETE /links).
     */
    public LinkResponse removeLink(long chatId, RemoveLinkRequest request) {
        log.atInfo()
                .addKeyValue("chatId", chatId)
                .addKeyValue("url", request.getLink())
                .log("Удаление ссылки из scrapper");
        try {
            return scrapperRestClient
                    .method(HttpMethod.DELETE)
                    .uri("/links")
                    .header(TG_CHAT_HEADER, String.valueOf(chatId))
                    .body(request)
                    .retrieve()
                    .body(LinkResponse.class);
        } catch (HttpClientErrorException e) {
            log.atError()
                    .addKeyValue("chatId", chatId)
                    .addKeyValue("url", request.getLink())
                    .addKeyValue("status", e.getStatusCode().value())
                    .log("Ошибка удаления ссылки");
            throw new ScrapperClientException("Ошибка удаления ссылки: " + e.getMessage(), e);
        }
    }
}
