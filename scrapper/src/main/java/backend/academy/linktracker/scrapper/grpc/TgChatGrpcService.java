package backend.academy.linktracker.scrapper.grpc;

import backend.academy.linktracker.grpc.TgChatRequest;
import backend.academy.linktracker.grpc.TgChatResponse;
import backend.academy.linktracker.grpc.TgChatServiceGrpc;
import backend.academy.linktracker.scrapper.exception.ChatAlreadyExistsException;
import backend.academy.linktracker.scrapper.exception.ChatNotFoundException;
import backend.academy.linktracker.scrapper.service.TgChatService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class TgChatGrpcService extends TgChatServiceGrpc.TgChatServiceImplBase {

    private final TgChatService tgChatService;

    @Override
    public void registerChat(TgChatRequest request, StreamObserver<TgChatResponse> responseObserver) {
        try {
            tgChatService.registerChat(request.getTgChatId());
            log.atInfo().addKeyValue("chatId", request.getTgChatId()).log("gRPC: чат зарегистрирован");
            responseObserver.onNext(TgChatResponse.newBuilder().setSuccess(true).build());
            responseObserver.onCompleted();
        } catch (ChatAlreadyExistsException e) {
            responseObserver.onError(Status.ALREADY_EXISTS
                    .withDescription("Чат уже зарегистрирован: " + request.getTgChatId())
                    .asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void deleteChat(TgChatRequest request, StreamObserver<TgChatResponse> responseObserver) {
        try {
            tgChatService.deleteChat(request.getTgChatId());
            log.atInfo().addKeyValue("chatId", request.getTgChatId()).log("gRPC: чат удалён");
            responseObserver.onNext(TgChatResponse.newBuilder().setSuccess(true).build());
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
}
