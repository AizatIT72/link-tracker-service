package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.enums.BotMenuCommandType;
import com.pengrad.telegrambot.Callback;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetMyCommands;
import com.pengrad.telegrambot.response.SendResponse;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Сервис взаимодействия с Telegram API.
 * Запускает слушатель обновлений и делегирует обработку {@link MessageHandler}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramMessageService {

    private final TelegramBot bot;
    private final MessageHandler messageHandler;

    @PostConstruct
    public void startBot() {
        setupMenu();
        bot.setUpdatesListener(
                updates -> {
                    for (Update update : updates) {
                        if (update.message() != null && update.message().text() != null) {
                            processUpdate(update);
                        }
                    }
                    return UpdatesListener.CONFIRMED_UPDATES_ALL;
                },
                e -> {
                    if (e.response() != null) {
                        log.atError()
                                .addKeyValue("error_code", e.response().errorCode())
                                .addKeyValue("error_description", e.response().description())
                                .log("Ошибка при получении обновления");
                    } else {
                        log.atError().setCause(e).log("Произошла сетевая ошибка");
                    }
                });
        log.atInfo().log("Telegram бот запущен");
    }

    public void processUpdate(Update update) {
        long chatId = update.message().chat().id();
        String text = update.message().text();

        log.atDebug().addKeyValue("chatId", chatId).addKeyValue("text", text).log("Получено сообщение");

        String response = messageHandler.handle(chatId, text);
        sendMessage(chatId, response);
    }

    public void sendMessage(long chatId, String textMessage) {
        SendMessage request = new SendMessage(chatId, textMessage);
        bot.execute(request, new Callback<SendMessage, SendResponse>() {
            @Override
            public void onResponse(SendMessage sendMessage, SendResponse sendResponse) {
                if (!sendResponse.isOk()) {
                    log.atError()
                            .addKeyValue("chatId", chatId)
                            .addKeyValue("description", sendResponse.description())
                            .log("Ошибка при отправке сообщения");
                }
            }

            @Override
            public void onFailure(SendMessage sendMessage, IOException e) {
                log.atError().addKeyValue("chatId", chatId).setCause(e).log("Сетевая ошибка при отправке");
            }
        });
    }

    private void setupMenu() {
        BotCommand[] commands = {
            new BotCommand(
                    BotMenuCommandType.START.getBotMenuCommand(), BotMenuCommandType.START.getDescriptionBotCommand()),
            new BotCommand(
                    BotMenuCommandType.HELP.getBotMenuCommand(), BotMenuCommandType.HELP.getDescriptionBotCommand()),
            new BotCommand(
                    BotMenuCommandType.TRACK.getBotMenuCommand(), BotMenuCommandType.TRACK.getDescriptionBotCommand()),
            new BotCommand(
                    BotMenuCommandType.UNTRACK.getBotMenuCommand(),
                    BotMenuCommandType.UNTRACK.getDescriptionBotCommand()),
            new BotCommand(
                    BotMenuCommandType.LIST.getBotMenuCommand(), BotMenuCommandType.LIST.getDescriptionBotCommand())
        };
        SetMyCommands myCommands = new SetMyCommands(commands);
        bot.execute(myCommands);
    }
}
