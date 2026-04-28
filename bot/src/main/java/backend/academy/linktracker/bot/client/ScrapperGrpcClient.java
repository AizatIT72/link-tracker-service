package backend.academy.linktracker.bot.client;

import backend.academy.linktracker.bot.dto.AddLinkRequest;
import backend.academy.linktracker.bot.dto.LinkResponse;
import backend.academy.linktracker.bot.dto.ListLinksResponse;
import backend.academy.linktracker.bot.dto.RemoveLinkRequest;
import backend.academy.linktracker.bot.exception.ScrapperClientException;
import backend.academy.linktracker.grpc.GetLinksRequest;
import backend.academy.linktracker.grpc.LinksServiceGrpc;
import backend.academy.linktracker.grpc.TgChatRequest;
import backend.academy.linktracker.grpc.TgChatServiceGrpc;
import io.grpc.StatusRuntimeException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * gRPC клиент для взаимодействия с Scrapper сервисом.
 * Заменяет REST ScrapperClient на gRPC вызовы.
 */
@Component
@Slf4j
public class ScrapperGrpcClient {

    private final TgChatServiceGrpc.TgChatServiceBlockingStub tgChatStub;
    private final LinksServiceGrpc.LinksServiceBlockingStub linksStub;

    public ScrapperGrpcClient(
            TgChatServiceGrpc.TgChatServiceBlockingStub tgChatStub,
            LinksServiceGrpc.LinksServiceBlockingStub linksStub) {
        this.tgChatStub = tgChatStub;
        this.linksStub = linksStub;
    }

    public void registerChat(long chatId) {
        log.atInfo().addKeyValue("chatId", chatId).log("gRPC: регистрация чата");
        try {
            tgChatStub.registerChat(
                    TgChatRequest.newBuilder().setTgChatId(chatId).build());
        } catch (StatusRuntimeException e) {
            throw new ScrapperClientException("Ошибка регистрации чата: " + e.getMessage(), e);
        }
    }

    public void deleteChat(long chatId) {
        log.atInfo().addKeyValue("chatId", chatId).log("gRPC: удаление чата");
        try {
            tgChatStub.deleteChat(TgChatRequest.newBuilder().setTgChatId(chatId).build());
        } catch (StatusRuntimeException e) {
            throw new ScrapperClientException("Ошибка удаления чата: " + e.getMessage(), e);
        }
    }

    public ListLinksResponse getLinks(long chatId) {
        log.atInfo().addKeyValue("chatId", chatId).log("gRPC: получение ссылок");
        try {
            var response = linksStub.getLinks(
                    GetLinksRequest.newBuilder().setTgChatId(chatId).build());
            List<LinkResponse> links = response.getLinksList().stream()
                    .map(l -> LinkResponse.builder()
                            .id(l.getId())
                            .url(l.getUrl())
                            .tags(l.getTagsList())
                            .filters(l.getFiltersList())
                            .build())
                    .toList();
            return new ListLinksResponse(links, links.size());
        } catch (StatusRuntimeException e) {
            throw new ScrapperClientException("Ошибка получения ссылок: " + e.getMessage(), e);
        }
    }

    public LinkResponse addLink(long chatId, AddLinkRequest request) {
        log.atInfo()
                .addKeyValue("chatId", chatId)
                .addKeyValue("url", request.getLink())
                .log("gRPC: добавление ссылки");
        try {
            var grpcRequest = backend.academy.linktracker.grpc.AddLinkRequest.newBuilder()
                    .setTgChatId(chatId)
                    .setUrl(request.getLink())
                    .addAllTags(request.getTags() != null ? request.getTags() : List.of())
                    .addAllFilters(request.getFilters() != null ? request.getFilters() : List.of())
                    .build();
            var response = linksStub.addLink(grpcRequest);
            return LinkResponse.builder()
                    .id(response.getId())
                    .url(response.getUrl())
                    .tags(response.getTagsList())
                    .filters(response.getFiltersList())
                    .build();
        } catch (StatusRuntimeException e) {
            throw new ScrapperClientException("Ошибка добавления ссылки: " + e.getMessage(), e);
        }
    }

    public LinkResponse removeLink(long chatId, RemoveLinkRequest request) {
        log.atInfo()
                .addKeyValue("chatId", chatId)
                .addKeyValue("url", request.getLink())
                .log("gRPC: удаление ссылки");
        try {
            var grpcRequest = backend.academy.linktracker.grpc.RemoveLinkRequest.newBuilder()
                    .setTgChatId(chatId)
                    .setUrl(request.getLink())
                    .build();
            var response = linksStub.removeLink(grpcRequest);
            return LinkResponse.builder()
                    .id(response.getId())
                    .url(response.getUrl())
                    .tags(response.getTagsList())
                    .filters(response.getFiltersList())
                    .build();
        } catch (StatusRuntimeException e) {
            throw new ScrapperClientException("Ошибка удаления ссылки: " + e.getMessage(), e);
        }
    }
}
