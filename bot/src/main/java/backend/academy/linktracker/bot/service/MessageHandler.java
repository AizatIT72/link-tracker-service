package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.client.ScrapperGrpcClient;
import backend.academy.linktracker.bot.dto.AddLinkRequest;
import backend.academy.linktracker.bot.dto.LinkResponse;
import backend.academy.linktracker.bot.dto.ListLinksResponse;
import backend.academy.linktracker.bot.dto.RemoveLinkRequest;
import backend.academy.linktracker.bot.enums.BotCommandType;
import backend.academy.linktracker.bot.exception.ScrapperClientException;
import backend.academy.linktracker.bot.model.UserState;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageHandler {

    private static final String HELP_TEXT = "Доступные команды:\n"
            + "/start — начать работу\n"
            + "/help — список команд\n"
            + "/track — начать отслеживание ссылки\n"
            + "/untrack — прекратить отслеживание ссылки\n"
            + "/list — список отслеживаемых ссылок";

    private final StateService stateService;
    private final ScrapperGrpcClient scrapperClient;

    public String handle(long chatId, String text) {
        UserState currentState = stateService.getState(chatId);
        BotCommandType command = BotCommandType.fromString(text);

        log.atDebug()
                .addKeyValue("chatId", chatId)
                .addKeyValue("state", currentState)
                .addKeyValue("command", command)
                .log("Обработка сообщения");

        if (command == BotCommandType.CANCEL) {
            stateService.reset(chatId);
            return BotCommandType.CANCEL.getMessage();
        }

        if (currentState != UserState.IDLE && isCommand(text) && command != BotCommandType.UNKNOWN) {
            stateService.reset(chatId);
        }

        return switch (stateService.getState(chatId)) {
            case IDLE -> handleIdle(chatId, text, command);
            case WAIT_LINK -> handleWaitLink(chatId, text);
            case WAIT_TAGS -> handleWaitTags(chatId, text);
        };
    }

    private String handleIdle(long chatId, String text, BotCommandType command) {
        return switch (command) {
            case START -> handleStart(chatId);
            case HELP -> HELP_TEXT;
            case TRACK -> {
                stateService.setState(chatId, UserState.WAIT_LINK);
                yield BotCommandType.TRACK.getMessage();
            }
            case UNTRACK -> handleUntrack(chatId, text);
            case LIST -> handleList(chatId, text);
            default -> BotCommandType.UNKNOWN.getMessage();
        };
    }

    private String handleStart(long chatId) {
        try {
            scrapperClient.registerChat(chatId);
            log.atInfo().addKeyValue("chatId", chatId).log("Чат зарегистрирован");
        } catch (ScrapperClientException e) {
            log.atInfo().addKeyValue("chatId", chatId).log("Чат уже зарегистрирован, продолжаем");
        }
        return BotCommandType.START.getMessage();
    }

    private String handleList(long chatId, String text) {
        String[] parts = text.trim().split("\\s+", 2);
        String filterTag = parts.length > 1 ? parts[1].trim() : null;

        try {
            ListLinksResponse response = scrapperClient.getLinks(chatId);
            if (response == null
                    || response.getLinks() == null
                    || response.getLinks().isEmpty()) {
                return "У вас нет отслеживаемых ссылок. Используйте /track, чтобы добавить ссылку.";
            }

            List<LinkResponse> links = response.getLinks();

            if (filterTag != null) {
                final String tag = filterTag;
                links = links.stream()
                        .filter(l -> l.getTags() != null && l.getTags().contains(tag))
                        .collect(Collectors.toList());
                if (links.isEmpty()) {
                    return "Нет ссылок с тегом \"" + tag + "\".";
                }
            }

            StringBuilder sb = new StringBuilder("Отслеживаемые ссылки:\n\n");
            for (LinkResponse link : links) {
                sb.append("🔗 ").append(link.getUrl()).append("\n");
                if (link.getTags() != null && !link.getTags().isEmpty()) {
                    sb.append("   Теги: ")
                            .append(String.join(", ", link.getTags()))
                            .append("\n");
                }
                sb.append("\n");
            }
            return sb.toString().trim();
        } catch (ScrapperClientException e) {
            log.atWarn().addKeyValue("chatId", chatId).setCause(e).log("Ошибка получения ссылок");
            return "Не удалось получить список ссылок. Попробуйте позже.";
        }
    }

    private String handleUntrack(long chatId, String text) {
        String[] parts = text.trim().split("\\s+", 2);
        if (parts.length > 1) {
            return doUntrack(chatId, parts[1].trim());
        }
        stateService.setState(chatId, UserState.WAIT_LINK);
        stateService.setPendingLink(chatId, "__UNTRACK__");
        return BotCommandType.UNTRACK.getMessage();
    }

    private String handleWaitLink(long chatId, String text) {
        String url = text.trim();

        String pendingLink = stateService.getPendingLink(chatId);
        if ("__UNTRACK__".equals(pendingLink)) {
            stateService.reset(chatId);
            return doUntrack(chatId, url);
        }

        if (!isValidUrl(url)) {
            return "Некорректная ссылка. Введите URL, начинающийся с http:// или https://:";
        }

        stateService.setPendingLink(chatId, url);
        stateService.setState(chatId, UserState.WAIT_TAGS);
        return "Введите теги через запятую (например: работа, github, баг) или /skip, чтобы пропустить:";
    }

    private String handleWaitTags(long chatId, String text) {
        String url = stateService.getPendingLink(chatId);
        List<String> tags = List.of();

        if (!"/skip".equalsIgnoreCase(text.trim())) {
            tags = Arrays.stream(text.split(","))
                    .map(String::trim)
                    .filter(t -> !t.isEmpty())
                    .collect(Collectors.toList());
        }

        stateService.reset(chatId);

        AddLinkRequest request =
                AddLinkRequest.builder().link(url).tags(tags).filters(List.of()).build();

        try {
            LinkResponse response = scrapperClient.addLink(chatId, request);
            StringBuilder sb = new StringBuilder("✅ Ссылка добавлена в отслеживание!\n")
                    .append("🔗 ")
                    .append(response.getUrl());
            if (response.getTags() != null && !response.getTags().isEmpty()) {
                sb.append("\nТеги: ").append(String.join(", ", response.getTags()));
            }
            return sb.toString();
        } catch (ScrapperClientException e) {
            Throwable cause = e.getCause();
            if (cause instanceof HttpClientErrorException hcee
                    && hcee.getStatusCode().value() == 409) {
                return "Ссылка уже отслеживается.";
            }
            log.atWarn()
                    .addKeyValue("chatId", chatId)
                    .addKeyValue("url", url)
                    .setCause(e)
                    .log("Ошибка добавления ссылки");
            return "Не удалось добавить ссылку. Попробуйте позже.";
        }
    }

    private String doUntrack(long chatId, String url) {
        if (!isValidUrl(url)) {
            return "Некорректная ссылка. Введите URL, начинающийся с http:// или https://.";
        }
        try {
            LinkResponse response = scrapperClient.removeLink(chatId, new RemoveLinkRequest(url));
            return "✅ Ссылка удалена из отслеживания:\n🔗 " + response.getUrl();
        } catch (ScrapperClientException e) {
            Throwable cause = e.getCause();
            if (cause instanceof HttpClientErrorException hcee
                    && hcee.getStatusCode().value() == 404) {
                return "Ссылка не найдена в вашем списке отслеживания.";
            }
            log.atWarn()
                    .addKeyValue("chatId", chatId)
                    .addKeyValue("url", url)
                    .setCause(e)
                    .log("Ошибка удаления ссылки");
            return "Не удалось удалить ссылку. Попробуйте позже.";
        }
    }

    private boolean isValidUrl(String url) {
        try {
            URI uri = URI.create(url);
            String scheme = uri.getScheme();
            return "http".equals(scheme) || "https".equals(scheme);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isCommand(String text) {
        return text != null && text.startsWith("/");
    }
}
