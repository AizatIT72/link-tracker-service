package backend.academy.linktracker.scrapper.scheduler;

import backend.academy.linktracker.scrapper.client.BotClient;
import backend.academy.linktracker.scrapper.client.GitHubClient;
import backend.academy.linktracker.scrapper.client.GitHubRepoResponse;
import backend.academy.linktracker.scrapper.client.StackOverflowClient;
import backend.academy.linktracker.scrapper.client.StackOverflowQuestionResponse;
import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import backend.academy.linktracker.scrapper.service.notification.MessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LinkUpdateScheduler {

    private final LinkRepository linkRepository;
    private final GitHubClient gitHubClient;
    private final StackOverflowClient stackOverflowClient;
    //private final BotClient botClient;
    private final MessageSender messageSender;

    @Scheduled(fixedDelayString = "${app.scheduler.interval:60000}")
    public void checkUpdates() {
        log.atInfo().log("Запуск проверки обновлений ссылок");

        List<Link> allLinks = linkRepository.findAll();
        log.atDebug().addKeyValue("linkCount", allLinks.size()).log("Количество ссылок для проверки");

        for (Link link : allLinks) {
            try {
                checkLink(link);
            } catch (Exception e) {
                log.atError().addKeyValue("url", link.getUrl()).setCause(e).log("Ошибка проверки ссылки");
            }
        }
    }

    private void checkLink(Link link) {
        if (gitHubClient.supports(link.getUrl())) {
            checkGitHubLink(link);
        } else if (stackOverflowClient.supports(link.getUrl())) {
            checkStackOverflowLink(link);
        } else {
            log.atDebug().addKeyValue("url", link.getUrl()).log("Ссылка не поддерживается ни одним клиентом");
        }
    }

    private void checkGitHubLink(Link link) {
        Optional<GitHubRepoResponse> responseOpt = gitHubClient.fetchRepository(link.getUrl());
        if (responseOpt.isEmpty()) {
            return;
        }

        GitHubRepoResponse response = responseOpt.orElseThrow();
        OffsetDateTime remoteUpdatedAt = response.pushedAt() != null ? response.pushedAt() : response.updatedAt();

        if (remoteUpdatedAt != null
                && link.getLastUpdatedAt() != null
                && remoteUpdatedAt.isAfter(link.getLastUpdatedAt())) {
            log.atInfo()
                    .addKeyValue("url", link.getUrl())
                    .addKeyValue("chatId", link.getChatId())
                    .log("Обнаружено обновление GitHub репозитория");

            sendUpdate(link, "Новые изменения в репозитории GitHub");
            link.setLastUpdatedAt(remoteUpdatedAt);
            linkRepository.update(link);
        }
    }

    private void checkStackOverflowLink(Link link) {
        Optional<StackOverflowQuestionResponse> responseOpt = stackOverflowClient.fetchQuestion(link.getUrl());
        if (responseOpt.isEmpty()) {
            return;
        }

        StackOverflowQuestionResponse response = responseOpt.orElseThrow();
        if (response.items() == null || response.items().isEmpty()) {
            return;
        }

        StackOverflowQuestionResponse.Item item = response.items().getFirst();
        OffsetDateTime remoteUpdatedAt = item.getLastActivityAsDateTime();

        if (remoteUpdatedAt != null
                && link.getLastUpdatedAt() != null
                && remoteUpdatedAt.isAfter(link.getLastUpdatedAt())) {
            log.atInfo()
                    .addKeyValue("url", link.getUrl())
                    .addKeyValue("chatId", link.getChatId())
                    .log("Обнаружено обновление StackOverflow вопроса");

            sendUpdate(link, "Новая активность на StackOverflow: " + item.title());
            link.setLastUpdatedAt(remoteUpdatedAt);
            linkRepository.update(link);
        }
    }

    private void sendUpdate(Link link, String description) {
        LinkUpdate update = LinkUpdate.builder()
                .id(link.getId())
                .url(link.getUrl())
                .description(description)
                .tgChatIds(List.of(link.getChatId()))
                .build();
        messageSender.sendMessage(update);
    }
}
