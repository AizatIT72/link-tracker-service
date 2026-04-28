package backend.academy.linktracker.scrapper.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import backend.academy.linktracker.scrapper.domain.Chat;
import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.repository.orm.OrmLinkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
@Transactional
@TestPropertySource(properties = "app.access-type=ORM")
class OrmLinkRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private LinkRepository linkRepository;

    @Autowired
    private ChatRepository chatRepository;

    private static final long CHAT_ID = 200L;
    private static final String URL = "https://stackoverflow.com";

    @BeforeEach
    void setUp() {
        chatRepository.save(new Chat(CHAT_ID));
    }

    @Test
    @DisplayName("ORM: Проверка реализации")
    void shouldInjectOrm() {
        assertThat(linkRepository).isInstanceOf(OrmLinkRepository.class);
    }

    @Test
    @DisplayName("ORM: Сценарий сохранения и удаления")
    void ormCrudFlow() {
        Link link = Link.builder().chatId(CHAT_ID).url(URL).build();
        linkRepository.save(link);
        assertThat(linkRepository.existsByChatIdAndUrl(CHAT_ID, URL)).isTrue();
        linkRepository.delete(CHAT_ID, URL);
        assertThat(linkRepository.existsByChatIdAndUrl(CHAT_ID, URL)).isFalse();
    }

    @Test
    @DisplayName("ORM: Ошибка на дубликат")
    void ormDuplicateError() {
        Link link = Link.builder().chatId(CHAT_ID).url(URL).build();
        linkRepository.save(link);
        assertThrows(DataIntegrityViolationException.class, () -> {
            linkRepository.save(link);
        });
    }
}
