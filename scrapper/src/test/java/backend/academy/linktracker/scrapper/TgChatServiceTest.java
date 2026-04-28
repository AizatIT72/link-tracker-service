package backend.academy.linktracker.scrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.domain.Chat;
import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.exception.ChatAlreadyExistsException;
import backend.academy.linktracker.scrapper.exception.ChatNotFoundException;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.service.TgChatService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TgChatServiceTest {

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private LinkRepository linkRepository;

    @InjectMocks
    private TgChatService tgChatService;

    private static final long CHAT_ID = 123L;

    @Test
    @DisplayName("registerChat — успешно регистрирует новый чат")
    void registerChat_shouldRegisterNewChat() {
        when(chatRepository.existsById(CHAT_ID)).thenReturn(false);

        tgChatService.registerChat(CHAT_ID);

        verify(chatRepository).save(any(Chat.class));
    }

    @Test
    @DisplayName("registerChat — бросает ChatAlreadyExistsException если чат уже существует")
    void registerChat_shouldThrowIfChatAlreadyExists() {
        when(chatRepository.existsById(CHAT_ID)).thenReturn(true);

        assertThatThrownBy(() -> tgChatService.registerChat(CHAT_ID)).isInstanceOf(ChatAlreadyExistsException.class);
    }

    @Test
    @DisplayName("deleteChat — успешно удаляет чат и все его ссылки")
    void deleteChat_shouldDeleteChatAndLinks() {
        when(chatRepository.existsById(CHAT_ID)).thenReturn(true);
        Link link1 = Link.builder()
                .chatId(CHAT_ID)
                .url("https://github.com/test/repo")
                .build();
        Link link2 = Link.builder()
                .chatId(CHAT_ID)
                .url("https://stackoverflow.com/questions/1")
                .build();
        when(linkRepository.findAllByChatId(CHAT_ID)).thenReturn(List.of(link1, link2));

        tgChatService.deleteChat(CHAT_ID);

        verify(linkRepository).delete(CHAT_ID, "https://github.com/test/repo");
        verify(linkRepository).delete(CHAT_ID, "https://stackoverflow.com/questions/1");
        verify(chatRepository).delete(CHAT_ID);
    }

    @Test
    @DisplayName("deleteChat — бросает ChatNotFoundException если чат не найден")
    void deleteChat_shouldThrowIfChatNotFound() {
        when(chatRepository.existsById(CHAT_ID)).thenReturn(false);

        assertThatThrownBy(() -> tgChatService.deleteChat(CHAT_ID)).isInstanceOf(ChatNotFoundException.class);
    }

    @Test
    @DisplayName("deleteChat — удаляет чат без ссылок")
    void deleteChat_shouldDeleteChatWithNoLinks() {
        when(chatRepository.existsById(CHAT_ID)).thenReturn(true);
        when(linkRepository.findAllByChatId(CHAT_ID)).thenReturn(List.of());

        tgChatService.deleteChat(CHAT_ID);

        verify(chatRepository).delete(CHAT_ID);
    }

    @Test
    @DisplayName("existsById — возвращает true если чат существует")
    void existsById_shouldReturnTrueIfExists() {
        when(chatRepository.existsById(CHAT_ID)).thenReturn(true);

        assertThat(tgChatService.existsById(CHAT_ID)).isTrue();
    }

    @Test
    @DisplayName("existsById — возвращает false если чат не существует")
    void existsById_shouldReturnFalseIfNotExists() {
        when(chatRepository.existsById(CHAT_ID)).thenReturn(false);

        assertThat(tgChatService.existsById(CHAT_ID)).isFalse();
    }
}
