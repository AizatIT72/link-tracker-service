package backend.academy.linktracker.scrapper.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import backend.academy.linktracker.scrapper.domain.Chat;
import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
@Transactional
@TestPropertySource(properties = "app.access-type=SQL")
class SqlLinkRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private LinkRepository linkRepository;

    @Autowired
    private ChatRepository chatRepository;

    private static final long CHAT_ID = 100L;
    private static final String URL = "https://github.com/spring-projects/spring-boot";

    @BeforeEach
    void setUp() {
        chatRepository.save(new Chat(CHAT_ID));
    }

    @Test
    @DisplayName("Успешное сохранение ссылки")
    void shouldSaveLinkSuccessfully() {
        Link link = Link.builder().chatId(CHAT_ID).url(URL).build();
        linkRepository.save(link);
        assertThat(linkRepository.existsByChatIdAndUrl(CHAT_ID, URL)).isTrue();
    }

    @Test
    @DisplayName("Ошибка при добавлении дубликата")
    void shouldThrowExceptionWhenLinkIsDuplicate() {
        Link link = Link.builder().chatId(CHAT_ID).url(URL).build();
        linkRepository.save(link);
        assertThrows(DuplicateKeyException.class, () -> linkRepository.save(link));
    }

    @Test
    @DisplayName("Успешное удаление ссылки")
    void shouldDeleteLinkSuccessfully() {
        Link link = Link.builder().chatId(CHAT_ID).url(URL).build();
        linkRepository.save(link);
        linkRepository.delete(CHAT_ID, URL);
        assertThat(linkRepository.existsByChatIdAndUrl(CHAT_ID, URL)).isFalse();
    }
}
