package backend.academy.linktracker.scrapper.grpc;

import backend.academy.linktracker.grpc.AddLinkRequest;
import backend.academy.linktracker.grpc.GetLinksRequest;
import backend.academy.linktracker.grpc.GetLinksResponse;
import backend.academy.linktracker.grpc.LinkResponse;
import backend.academy.linktracker.grpc.LinksServiceGrpc;
import backend.academy.linktracker.grpc.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.exception.ChatNotFoundException;
import backend.academy.linktracker.scrapper.exception.LinkAlreadyExistsException;
import backend.academy.linktracker.scrapper.exception.LinkNotFoundException;
import backend.academy.linktracker.scrapper.service.LinkService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class LinksGrpcService extends LinksServiceGrpc.LinksServiceImplBase {

    private final LinkService linkService;

    @Override
    public void getLinks(GetLinksRequest request, StreamObserver<GetLinksResponse> responseObserver) {
        try {
            List<Link> links = linkService.getLinks(request.getTgChatId());
            GetLinksResponse.Builder builder = GetLinksResponse.newBuilder();
            for (Link link : links) {
                builder.addLinks(toProto(link));
            }
            builder.setSize(links.size());
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (ChatNotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Чат не найден: " + request.getTgChatId())
                    .asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void addLink(AddLinkRequest request, StreamObserver<LinkResponse> responseObserver) {
        try {
            Link link = linkService.addLink(
                    request.getTgChatId(), request.getUrl(), request.getTagsList(), request.getFiltersList());
            responseObserver.onNext(toLinkResponse(link));
            responseObserver.onCompleted();
        } catch (ChatNotFoundException e) {
            responseObserver.onError(
                    Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (LinkAlreadyExistsException e) {
            responseObserver.onError(
                    Status.ALREADY_EXISTS.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void removeLink(RemoveLinkRequest request, StreamObserver<LinkResponse> responseObserver) {
        try {
            Link link = linkService.removeLink(request.getTgChatId(), request.getUrl());
            responseObserver.onNext(toLinkResponse(link));
            responseObserver.onCompleted();
        } catch (ChatNotFoundException e) {
            responseObserver.onError(
                    Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (LinkNotFoundException e) {
            responseObserver.onError(
                    Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    private backend.academy.linktracker.grpc.LinkProto toProto(Link link) {
        return backend.academy.linktracker.grpc.LinkProto.newBuilder()
                .setId(link.getId())
                .setUrl(link.getUrl())
                .addAllTags(link.getTags() != null ? link.getTags() : List.of())
                .addAllFilters(link.getFilters() != null ? link.getFilters() : List.of())
                .build();
    }

    private LinkResponse toLinkResponse(Link link) {
        return LinkResponse.newBuilder()
                .setId(link.getId())
                .setUrl(link.getUrl())
                .addAllTags(link.getTags() != null ? link.getTags() : List.of())
                .addAllFilters(link.getFilters() != null ? link.getFilters() : List.of())
                .build();
    }
}
