package backend.academy.linktracker.bot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.enums.BotCommandType;
import backend.academy.linktracker.bot.service.MessageHandler;
import backend.academy.linktracker.bot.service.TelegramMessageService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class TelegramMessageServiceTest {

    private TelegramBot bot;
    private TelegramMessageService service;
    private MessageHandler messageHandler;

    @BeforeEach
    void setUp() {
        bot = mock(TelegramBot.class);
        messageHandler = mock(MessageHandler.class);
        service = new TelegramMessageService(bot, messageHandler);
    }

    @Test
    @DisplayName("При получении /start бот отвечает приветствием")
    void testShouldReturnWelcomeStart() {
        when(messageHandler.handle(123L, "/start")).thenReturn(BotCommandType.START.getMessage());
        Update update = createMockUpdate("/start", 123L);
        service.processUpdate(update);
        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(bot).execute(captor.capture(), any());
        assertEquals(
                BotCommandType.START.getMessage(),
                captor.getValue().getParameters().get("text"));
    }

    @Test
    @DisplayName("При получении /help бот отвечает списком команд")
    void testShouldReturnHelpCommands() {
        when(messageHandler.handle(456L, "/help")).thenReturn(BotCommandType.HELP.getMessage());
        Update update = createMockUpdate("/help", 456L);
        service.processUpdate(update);
        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(bot).execute(captor.capture(), any());
        assertEquals(
                BotCommandType.HELP.getMessage(),
                captor.getValue().getParameters().get("text"));
    }

    @Test
    @DisplayName("При получении неизвестной команды бот отвечает ошибкой")
    void testShouldReturnErrorUnknownCommand() {
        when(messageHandler.handle(789L, "Случайный текст")).thenReturn(BotCommandType.UNKNOWN.getMessage());
        Update update = createMockUpdate("Случайный текст", 789L);
        service.processUpdate(update);
        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(bot).execute(captor.capture(), any());
        assertEquals(
                BotCommandType.UNKNOWN.getMessage(),
                captor.getValue().getParameters().get("text"));
    }

    private Update createMockUpdate(String text, Long chatId) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);

        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn(text);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);

        return update;
    }
}
