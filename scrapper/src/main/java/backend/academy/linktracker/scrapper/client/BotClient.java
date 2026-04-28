package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.properties.BotProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class BotClient {

    private final RestClient restClient;

    public BotClient(BotProperties botProperties) {
        this.restClient = RestClient.builder()
                .baseUrl(botProperties.getBaseUrl())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public void sendUpdate(LinkUpdate update) {
        log.atInfo()
                .addKeyValue("linkId", update.getId())
                .addKeyValue("url", update.getUrl())
                .addKeyValue("chatCount", update.getTgChatIds().size())
                .log("Отправка обновления в Bot");
        try {
            restClient.post().uri("/updates").body(update).retrieve().toBodilessEntity();
        } catch (HttpClientErrorException e) {
            log.atError()
                    .addKeyValue("url", update.getUrl())
                    .addKeyValue("status", e.getStatusCode().value())
                    .log("Ошибка отправки обновления в Bot");
        } catch (Exception e) {
            log.atError()
                    .addKeyValue("url", update.getUrl())
                    .setCause(e)
                    .log("Неожиданная ошибка при отправке обновления в Bot");
        }
    }
}
