package backend.academy.linktracker.bot.configuration;

import backend.academy.linktracker.grpc.LinksServiceGrpc;
import backend.academy.linktracker.grpc.TgChatServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация gRPC канала для подключения к Scrapper.
 */
@Configuration
public class GrpcClientConfiguration {

    @Value("${app.scrapper.grpc-host:localhost}")
    private String grpcHost;

    @Value("${app.scrapper.grpc-port:9090}")
    private int grpcPort;

    @Bean
    public ManagedChannel scrapperGrpcChannel() {
        return ManagedChannelBuilder.forAddress(grpcHost, grpcPort)
                .usePlaintext()
                .build();
    }

    @Bean
    public TgChatServiceGrpc.TgChatServiceBlockingStub tgChatServiceStub(ManagedChannel channel) {
        return TgChatServiceGrpc.newBlockingStub(channel);
    }

    @Bean
    public LinksServiceGrpc.LinksServiceBlockingStub linksServiceStub(ManagedChannel channel) {
        return LinksServiceGrpc.newBlockingStub(channel);
    }
}
