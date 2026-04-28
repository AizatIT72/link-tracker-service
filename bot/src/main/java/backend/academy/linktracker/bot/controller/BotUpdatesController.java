package backend.academy.linktracker.bot.controller;

import backend.academy.linktracker.bot.dto.ApiErrorResponse;
import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.bot.service.TelegramMessageService;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST-контроллер для приёма обновлений от Scrapper.
 * POST /updates — соответствует OpenAPI контракту bot-api.yaml.
 * Scrapper вызывает этот endpoint, когда обнаруживает обновление по ссылке.
 */
@RestController
@RequestMapping("/updates")
@RequiredArgsConstructor
@Slf4j
public class BotUpdatesController {

    private final TelegramMessageService telegramMessageService;

    @PostMapping
    public ResponseEntity<Void> receiveUpdate(@Valid @RequestBody LinkUpdate linkUpdate) {
        log.atInfo()
                .addKeyValue("linkId", linkUpdate.getId())
                .addKeyValue("url", linkUpdate.getUrl())
                .addKeyValue("chatCount", linkUpdate.getTgChatIds().size())
                .log("Получено обновление от scrapper");

        String message = "🔔 Обновление по ссылке!\n🔗 " + linkUpdate.getUrl()
                + (linkUpdate.getDescription() != null ? "\n📝 " + linkUpdate.getDescription() : "");

        for (Long chatId : linkUpdate.getTgChatIds()) {
            telegramMessageService.sendMessage(chatId, message);
        }

        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        log.atWarn().setCause(ex).log("Некорректный запрос к /updates");
        ApiErrorResponse error = ApiErrorResponse.builder()
                .description("Некорректные параметры запроса")
                .code("400")
                .exceptionName(ex.getClass().getSimpleName())
                .exceptionMessage(ex.getMessage())
                .stacktrace(Arrays.stream(ex.getStackTrace())
                        .map(StackTraceElement::toString)
                        .collect(Collectors.toList()))
                .build();
        return ResponseEntity.badRequest().body(error);
    }
}
