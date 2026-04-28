package backend.academy.linktracker.scrapper.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
class DatabaseMigrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Миграции: Проверка создания структуры таблиц на чистой БД")
    void shouldHaveCorrectTableStructure() {
        Boolean chatsExist = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'chats')", Boolean.class);
        Boolean linksExist = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'links')", Boolean.class);
        Boolean tagsExist = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'link_tags')", Boolean.class);
        assertThat(chatsExist).as("Таблица 'chats' должна существовать").isTrue();
        assertThat(linksExist).as("Таблица 'links' должна существовать").isTrue();
        assertThat(tagsExist).as("Таблица 'link_tags' должна существовать").isTrue();
    }

    @Test
    @DisplayName("Миграции: Проверка наличия ожидаемых колонок в таблице links")
    void shouldHaveCorrectColumnsInLinksTable() {
        var columns = jdbcTemplate.queryForList(
                "SELECT column_name FROM information_schema.columns WHERE table_name = 'links'", String.class);
        assertThat(columns).containsExactlyInAnyOrder("id", "chat_id", "url", "last_checked_at", "last_updated_at");
    }
}
