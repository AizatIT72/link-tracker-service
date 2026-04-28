package backend.academy.linktracker.scrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.exception.ChatNotFoundException;
import backend.academy.linktracker.scrapper.exception.LinkAlreadyExistsException;
import backend.academy.linktracker.scrapper.exception.LinkNotFoundException;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.service.LinkService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LinkServiceTest {

    @Mock
    private LinkRepository linkRepository;

    @Mock
    private ChatRepository chatRepository;

    @InjectMocks
    private LinkService linkService;

    private static final long CHAT_ID = 123L;
    private static final String URL = "https://github.com/test/repo";

    @BeforeEach
    void setUp() {
        lenient().when(chatRepository.existsById(CHAT_ID)).thenReturn(true);
    }

    @Test
    @DisplayName("getLinks — возвращает список ссылок чата")
    void getLinks_shouldReturnLinks() {
        Link link = Link.builder().chatId(CHAT_ID).url(URL).build();
        when(linkRepository.findAllByChatId(CHAT_ID)).thenReturn(List.of(link));

        List<Link> result = linkService.getLinks(CHAT_ID);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getUrl()).isEqualTo(URL);
    }

    @Test
    @DisplayName("getLinks — бросает ChatNotFoundException если чат не найден")
    void getLinks_shouldThrowIfChatNotFound() {
        when(chatRepository.existsById(CHAT_ID)).thenReturn(false);

        assertThatThrownBy(() -> linkService.getLinks(CHAT_ID)).isInstanceOf(ChatNotFoundException.class);
    }

    @Test
    @DisplayName("addLink — успешно добавляет ссылку")
    void addLink_shouldAddLink() {
        when(linkRepository.existsByChatIdAndUrl(CHAT_ID, URL)).thenReturn(false);
        Link saved = Link.builder().id(1L).chatId(CHAT_ID).url(URL).build();
        when(linkRepository.save(any())).thenReturn(saved);

        Link result = linkService.addLink(CHAT_ID, URL, List.of("tag1"), List.of());

        assertThat(result.getUrl()).isEqualTo(URL);
        assertThat(result.getId()).isEqualTo(1L);
        verify(linkRepository).save(any());
    }

    @Test
    @DisplayName("addLink — бросает LinkAlreadyExistsException если ссылка уже есть")
    void addLink_shouldThrowIfLinkExists() {
        when(linkRepository.existsByChatIdAndUrl(CHAT_ID, URL)).thenReturn(true);

        assertThatThrownBy(() -> linkService.addLink(CHAT_ID, URL, List.of(), List.of()))
                .isInstanceOf(LinkAlreadyExistsException.class);
    }

    @Test
    @DisplayName("addLink — бросает ChatNotFoundException если чат не найден")
    void addLink_shouldThrowIfChatNotFound() {
        when(chatRepository.existsById(CHAT_ID)).thenReturn(false);

        assertThatThrownBy(() -> linkService.addLink(CHAT_ID, URL, List.of(), List.of()))
                .isInstanceOf(ChatNotFoundException.class);
    }

    @Test
    @DisplayName("removeLink — успешно удаляет ссылку")
    void removeLink_shouldRemoveLink() {
        Link existing = Link.builder().id(1L).chatId(CHAT_ID).url(URL).build();
        when(linkRepository.findByChatIdAndUrl(CHAT_ID, URL)).thenReturn(Optional.of(existing));

        Link result = linkService.removeLink(CHAT_ID, URL);

        assertThat(result.getUrl()).isEqualTo(URL);
        verify(linkRepository).delete(CHAT_ID, URL);
    }

    @Test
    @DisplayName("removeLink — бросает LinkNotFoundException если ссылка не найдена")
    void removeLink_shouldThrowIfLinkNotFound() {
        when(linkRepository.findByChatIdAndUrl(CHAT_ID, URL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> linkService.removeLink(CHAT_ID, URL)).isInstanceOf(LinkNotFoundException.class);
    }

    @Test
    @DisplayName("removeLink — бросает ChatNotFoundException если чат не найден")
    void removeLink_shouldThrowIfChatNotFound() {
        when(chatRepository.existsById(CHAT_ID)).thenReturn(false);

        assertThatThrownBy(() -> linkService.removeLink(CHAT_ID, URL)).isInstanceOf(ChatNotFoundException.class);
    }

    @Test
    @DisplayName("getAllLinks — возвращает все ссылки")
    void getAllLinks_shouldReturnAll() {
        Link link1 = Link.builder().chatId(CHAT_ID).url(URL).build();
        Link link2 = Link.builder()
                .chatId(456L)
                .url("https://stackoverflow.com/questions/1")
                .build();
        when(linkRepository.findAll()).thenReturn(List.of(link1, link2));

        List<Link> result = linkService.getAllLinks();

        assertThat(result).hasSize(2);
    }
}
