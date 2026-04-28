package backend.academy.linktracker.scrapper.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class EndToEndTest {

    private static final Logger log = LoggerFactory.getLogger(EndToEndTest.class);
    private static final Network NETWORK = Network.newNetwork();
    private static final java.nio.file.Path PROJECT_ROOT =
            Paths.get("").toAbsolutePath().getParent();

    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17")
            .withNetwork(NETWORK)
            .withNetworkAliases("db") // Скраппер будет искать хост "db"
            .withDatabaseName("linktrackerDB")
            .withUsername("postgres")
            .withPassword("password");

    private static final GenericContainer<?> SCRAPPER = new GenericContainer<>(
                    new ImageFromDockerfile("localhost/scrapper-test", false)
                            .withDockerfileFromBuilder(builder -> builder.from("eclipse-temurin:25-jre")
                                    .workDir("/app")
                                    .copy("app.jar", "app.jar")
                                    .entryPoint("java", "-jar", "app.jar")
                                    .build())
                            .withFileFromPath("app.jar", PROJECT_ROOT.resolve("scrapper/target/scrapper-0.0.1.jar")))
            .withNetwork(NETWORK)
            .withNetworkAliases("scrapper")
            .withExposedPorts(8081)
            .withEnv("GITHUB_TOKEN", "test-token")
            .withEnv("STACKOVERFLOW_KEY", "test-key")
            .withEnv("STACKOVERFLOW_ACCESS_KEY", "none")
            .withEnv("app.bot.base-url", "http://bot:8080")
            .withEnv("spring.grpc.server.port", "9090")
            .withEnv("management.endpoints.web.exposure.include", "health")
            .withEnv("SPRING_DATASOURCE_URL", "jdbc:postgresql://db:5432/linktrackerDB")
            .withEnv("SPRING_DATASOURCE_USERNAME", "postgres")
            .withEnv("SPRING_DATASOURCE_PASSWORD", "password")
            .withLogConsumer(new Slf4jLogConsumer(log).withPrefix("SCRAPPER"))
            .waitingFor(Wait.forHttp("/actuator/health").forPort(8081).withStartupTimeout(Duration.ofMinutes(3)));

    private static final GenericContainer<?> BOT = new GenericContainer<>(
                    new ImageFromDockerfile("localhost/bot-test", false)
                            .withDockerfileFromBuilder(builder -> builder.from("eclipse-temurin:25-jre")
                                    .workDir("/app")
                                    .copy("app.jar", "app.jar")
                                    .entryPoint("java", "-jar", "app.jar")
                                    .build())
                            .withFileFromPath("app.jar", PROJECT_ROOT.resolve("bot/target/bot-0.0.1.jar")))
            .withNetwork(NETWORK)
            .withNetworkAliases("bot")
            .withExposedPorts(8080)
            .withEnv("TELEGRAM_TOKEN", "fake:token")
            .withEnv("app.scrapper.base-url", "http://scrapper:8081")
            .withEnv("app.scrapper.grpc-host", "scrapper")
            .withEnv("app.scrapper.grpc-port", "9090")
            .withEnv("management.endpoints.web.exposure.include", "health")
            .withLogConsumer(new Slf4jLogConsumer(log).withPrefix("BOT"))
            .waitingFor(Wait.forHttp("/actuator/health").forPort(8080).withStartupTimeout(Duration.ofMinutes(3)));

    static {
        POSTGRES.start();
        SCRAPPER.start();
        BOT.start();
    }

    private RestClient scrapperClient;
    private RestClient botClient;
    private static final long TEST_CHAT_ID = 999999L;

    @BeforeEach
    void setUp() {
        scrapperClient = RestClient.builder()
                .baseUrl("http://" + SCRAPPER.getHost() + ":" + SCRAPPER.getMappedPort(8081))
                .defaultHeader("Content-Type", "application/json")
                .build();

        botClient = RestClient.builder()
                .baseUrl("http://" + BOT.getHost() + ":" + BOT.getMappedPort(8080))
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Test
    @DisplayName("Scrapper health check — сервис запущен")
    void scrapperShouldBeHealthy() {
        var response = scrapperClient.get().uri("/actuator/health").retrieve().toEntity(String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("UP");
    }

    @Test
    @DisplayName("Bot health check — сервис запущен")
    void botShouldBeHealthy() {
        var response = botClient.get().uri("/actuator/health").retrieve().toEntity(String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("UP");
    }

    @Test
    @DisplayName("Регистрация чата — возвращает 200")
    void registerChatShouldReturn200() {
        var response = scrapperClient
                .post()
                .uri("/tg-chat/{id}", TEST_CHAT_ID)
                .retrieve()
                .toBodilessEntity();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("Повторная регистрация чата — возвращает 409")
    void registerChatDuplicateShouldReturn409() {
        long chatId = TEST_CHAT_ID + 1;
        scrapperClient.post().uri("/tg-chat/{id}", chatId).retrieve().toBodilessEntity();

        assertThatThrownBy(() -> scrapperClient
                        .post()
                        .uri("/tg-chat/{id}", chatId)
                        .retrieve()
                        .toBodilessEntity())
                .isInstanceOf(HttpClientErrorException.Conflict.class);
    }

    @Test
    @DisplayName("Полный flow: регистрация → добавление ссылки → получение списка")
    void addLinkFullFlow() {
        long chatId = TEST_CHAT_ID + 2;
        scrapperClient.post().uri("/tg-chat/{id}", chatId).retrieve().toBodilessEntity();

        var addBody = Map.of(
                "link", "https://github.com/microsoft/vscode",
                "tags", List.of("test"),
                "filters", List.of());

        var addResponse = scrapperClient
                .post()
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .body(addBody)
                .retrieve()
                .toEntity(String.class);

        assertThat(addResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(addResponse.getBody()).contains("vscode");

        var listResponse = scrapperClient
                .get()
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .retrieve()
                .toEntity(String.class);

        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listResponse.getBody()).contains("vscode");
    }

    @Test
    @DisplayName("Получение ссылок незарегистрированного чата — 404")
    void getLinksUnknownChatShouldReturn404() {
        assertThatThrownBy(() -> scrapperClient
                        .get()
                        .uri("/links")
                        .header("Tg-Chat-Id", "0")
                        .retrieve()
                        .toEntity(String.class))
                .isInstanceOf(HttpClientErrorException.NotFound.class);
    }

    @Test
    @DisplayName("Bot принимает POST /updates — возвращает 200")
    void botShouldAcceptUpdate() {
        var updateBody = Map.of(
                "id",
                1,
                "url",
                "https://github.com/microsoft/vscode",
                "description",
                "Новые изменения",
                "tgChatIds",
                List.of(123456789L));

        var response =
                botClient.post().uri("/updates").body(updateBody).retrieve().toBodilessEntity();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("Удаление чата — возвращает 200")
    void deleteChatShouldReturn200() {
        long chatId = TEST_CHAT_ID + 3;
        scrapperClient.post().uri("/tg-chat/{id}", chatId).retrieve().toBodilessEntity();

        var response =
                scrapperClient.delete().uri("/tg-chat/{id}", chatId).retrieve().toBodilessEntity();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
