package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.properties.GithubProperties;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class GitHubClient {

    private static final Pattern GITHUB_PATTERN =
            Pattern.compile("https?://github\\.com/([^/]+)/([^/]+?)(?:\\.git)?(?:/.*)?$");

    private final RestClient restClient;

    public GitHubClient(GithubProperties githubProperties) {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeader("Authorization", "Bearer " + githubProperties.getToken())
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .build();
    }

    public Optional<GitHubRepoResponse> fetchRepository(String url) {
        Matcher matcher = GITHUB_PATTERN.matcher(url);
        if (!matcher.matches()) {
            return Optional.empty();
        }

        String owner = matcher.group(1);
        String repo = matcher.group(2);

        log.atDebug().addKeyValue("owner", owner).addKeyValue("repo", repo).log("Запрос к GitHub API");

        try {
            GitHubRepoResponse response = restClient
                    .get()
                    .uri("/repos/{owner}/{repo}", owner, repo)
                    .retrieve()
                    .body(GitHubRepoResponse.class);
            return Optional.ofNullable(response);
        } catch (HttpClientErrorException e) {
            log.atWarn()
                    .addKeyValue("url", url)
                    .addKeyValue("status", e.getStatusCode().value())
                    .log("Ошибка запроса к GitHub API");
            return Optional.empty();
        } catch (Exception e) {
            log.atError().addKeyValue("url", url).setCause(e).log("Неожиданная ошибка при запросе к GitHub API");
            return Optional.empty();
        }
    }

    public List<GitHubIssueResponse> fetchIssues(String url) {
        Matcher matcher = GITHUB_PATTERN.matcher(url);
        if (!matcher.matches()) {
            return List.of();
        }

        String owner = matcher.group(1);
        String repo = matcher.group(2);

        log.atDebug().addKeyValue("owner", owner).addKeyValue("repo", repo).log("Запрос к GitHub API");
        try {
            GitHubIssueResponse[] response = restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                                .path("/repos/{owner}/{repo}/issues")
                                .queryParam("state", "all")
                                .queryParam("per_page", 100)
                                .build(owner, repo)
                )
                .retrieve()
                .body(GitHubIssueResponse[].class);
            return response == null ? List.of() : Arrays.asList(response);
        } catch (HttpClientErrorException e) {
            log.atWarn()
                .addKeyValue("url", url)
                .addKeyValue("status", e.getStatusCode().value())
                .log("Ошибка запроса Issues к GitHub API");
            return List.of();
        } catch (Exception e) {
            log.atError().addKeyValue("url", url).setCause(e).log("Неожиданная ошибка при запросе Issues");
            return List.of();
        }
    }

    public List<GitHubPullRequestResponse> fetchPullRequests(String url) {
        Matcher matcher = GITHUB_PATTERN.matcher(url);
        if (!matcher.matches()) {
            return List.of();
        }

        String owner = matcher.group(1);
        String repo = matcher.group(2);

        log.atDebug().addKeyValue("owner", owner).addKeyValue("repo", repo).log("Запрос к GitHub API");
        try {
            GitHubPullRequestResponse[] response = restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                    .path("/repos/{owner}/{repo}/pulls")
                    .queryParam("state", "all")
                    .queryParam("per_page", 100)
                    .build(owner, repo)
                )
                .retrieve()
                .body(GitHubPullRequestResponse[].class);
            return response == null ? List.of() : Arrays.asList(response);
        } catch (HttpClientErrorException e) {
            log.atWarn()
                .addKeyValue("url", url)
                .addKeyValue("status", e.getStatusCode().value())
                .log("Ошибка запроса Issues к GitHub API");
            return List.of();
        } catch (Exception e) {
            log.atError().addKeyValue("url", url).setCause(e).log("Неожиданная ошибка при запросе Issues");
            return List.of();
        }
    }

    public boolean supports(String url) {
        return GITHUB_PATTERN.matcher(url).matches();
    }
}
