package backend.academy.linktracker.scrapper.service.update;

import backend.academy.linktracker.scrapper.client.StackOverflowAnswerResponse;
import backend.academy.linktracker.scrapper.client.StackOverflowClient;
import backend.academy.linktracker.scrapper.client.StackOverflowCommentResponse;
import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.dto.UpdateNotification;
import backend.academy.linktracker.scrapper.properties.SchedulerProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StackOverflowUpdateChecker implements UpdateChecker {

    private final StackOverflowClient stackOverflowClient;
    private final SchedulerProperties properties;

    @Override
    public boolean supports(String url) {
        return stackOverflowClient.supports(url);
    }

    @Override
    public List<UpdateNotification> checkForUpdates(Link link) {
        List<UpdateNotification> notifications = new ArrayList<>();
        OffsetDateTime since = link.getLastUpdatedAt();
        String url = link.getUrl();

        String questionTitle = stackOverflowClient.fetchQuestion(url)
            .flatMap(q -> q.items() == null || q.items().isEmpty()
                ? java.util.Optional.empty()
                : java.util.Optional.ofNullable(q.items().getFirst().title()))
            .orElse("(unknown question)");

        stackOverflowClient.fetchAnswers(url).ifPresent(response -> {
            if (response.items() == null) return;
            for (StackOverflowAnswerResponse.Item item : response.items()) {
                OffsetDateTime createdAt = item.getCreatedAt();
                if (createdAt != null && (since == null || createdAt.isAfter(since))) {
                    notifications.add(UpdateNotification.builder()
                        .message(formatAnswer(questionTitle, item))
                        .eventTime(createdAt)
                        .build());
                }
            }
        });

        stackOverflowClient.fetchComments(url).ifPresent(response -> {
            if (response.items() == null) return;
            for (StackOverflowCommentResponse.Item item : response.items()) {
                OffsetDateTime createdAt = item.getCreatedAt();
                if (createdAt != null && (since == null || createdAt.isAfter(since))) {
                    notifications.add(UpdateNotification.builder()
                        .message(formatComment(questionTitle, item))
                        .eventTime(createdAt)
                        .build());
                }
            }
        });

        return notifications;
    }

    private String formatAnswer(String questionTitle, StackOverflowAnswerResponse.Item item) {
        return String.format("""
            🔔 Новый ответ на StackOverflow

            📌 Вопрос: %s
            👤 Автор: %s
            🕐 Создано: %s

            Содержимое: %s""",
            questionTitle,
            item.owner() != null ? item.owner().displayName() : "unknown",
            item.getCreatedAt(),
            preview(item.body()));
    }

    private String formatComment(String questionTitle, StackOverflowCommentResponse.Item item) {
        return String.format("""
            🔔 Новый комментарий на StackOverflow

            📌 Вопрос: %s
            👤 Автор: %s
            🕐 Создано: %s

            Содержимое: %s""",
            questionTitle,
            item.owner() != null ? item.owner().displayName() : "unknown",
            item.getCreatedAt(),
            preview(item.body()));
    }

    private String preview(String body) {
        if (body == null || body.isEmpty()) return "(empty)";
        int previewLength = properties.getPreviewLength();
        return body.length() <= previewLength ? body : body.substring(0, previewLength) + "...";
    }
}
