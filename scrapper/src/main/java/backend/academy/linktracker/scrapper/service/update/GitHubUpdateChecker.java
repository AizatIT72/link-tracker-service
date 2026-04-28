package backend.academy.linktracker.scrapper.service.update;

import backend.academy.linktracker.scrapper.client.GitHubClient;
import backend.academy.linktracker.scrapper.client.GitHubIssueResponse;
import backend.academy.linktracker.scrapper.client.GitHubPullRequestResponse;
import backend.academy.linktracker.scrapper.domain.Link;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import backend.academy.linktracker.scrapper.dto.UpdateNotification;
import backend.academy.linktracker.scrapper.properties.SchedulerProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GitHubUpdateChecker implements UpdateChecker {

    private final GitHubClient gitHubClient;
    private final SchedulerProperties properties;

    @Override
    public boolean supports(String url) {
        return gitHubClient.supports(url);
    }

    @Override
    public List<UpdateNotification> checkForUpdates(Link link) {
        List<UpdateNotification> notifications = new ArrayList<>();
        OffsetDateTime since = link.getLastUpdatedAt();

        for (GitHubIssueResponse issue : gitHubClient.fetchIssues(link.getUrl())) {
            if (since == null || issue.createdAt().isAfter(since)) {
                notifications.add(UpdateNotification.builder()
                    .message(formatIssue(issue))
                    .eventTime(issue.createdAt())
                    .build());
            }
        }

        for (GitHubPullRequestResponse pr : gitHubClient.fetchPullRequests(link.getUrl())) {
            if (since == null || pr.createdAt().isAfter(since)) {
                notifications.add(UpdateNotification.builder()
                    .message(formatPullRequest(pr))
                    .eventTime(pr.createdAt())
                    .build());
            }
        }
        return notifications;
    }

    private String formatIssue(GitHubIssueResponse issue) {
        return String.format("""
            🔔 Новый Issue

            📌 Заголовок: %s
            👤 Автор: %s
            🕐 Создано: %s

            Содержимое: %s""",
            issue.title(),
            issue.user() != null ? issue.user().login() : "unknown",
            issue.createdAt(),
            preview(issue.body()));
    }

    private String formatPullRequest(GitHubPullRequestResponse pr) {
        return String.format("""
            🔔 Новый Pull Request

            📌 Заголовок: %s
            👤 Автор: %s
            🕐 Создано: %s

            Содержимое: %s""",
            pr.title(),
            pr.user() != null ? pr.user().login() : "unknown",
            pr.createdAt(),
            preview(pr.body()));
    }

    private String preview(String body) {
        if (body == null || body.isEmpty()) return "(empty)";
        int previewLength = properties.getPreviewLength();
        return body.length() <= previewLength ? body : body.substring(0, previewLength) + "...";
    }
}
