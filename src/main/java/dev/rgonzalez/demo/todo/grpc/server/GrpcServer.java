package dev.rgonzalez.demo.todo.grpc.server;

import dev.rgonzalez.demo.todo.AppProperties;
import dev.rgonzalez.demo.todo.grpc.service.TodoGrpcService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class GrpcServer {
    private final TodoGrpcService todoGrpcService;
    private final AppProperties appProperties;
    private final ExecutorService grpcExecutor;
    private final ErrorHandlingInterceptor errorHandlingInterceptor;
    public static final int MAX_INBOUND_MESSAGE_SIZE = (100 * 1024 * 1024);
    private Server server;

    @PostConstruct
    void init() {
        grpcExecutor.execute(() -> {
            try {
                server = createGrpcServer().start();
                log.info("gRPC server started, listening on {}", server.getPort());
            } catch (IOException e) {
                throw new BeanCreationException("Error starting gRPC server", e);
            }
        });
    }

    @PreDestroy
    void shutdown() {
        log.info("Starting gRPC Server shutdown with grace period {}s", appProperties.getTerminationGracePeriodSeconds());
        try {
            if (!server.shutdown().awaitTermination(appProperties.getTerminationGracePeriodSeconds(), TimeUnit.SECONDS)) {
                server.shutdownNow();
            }
        } catch (InterruptedException e) {
            server.shutdownNow();
            Thread.currentThread().interrupt();
        } finally {
            log.info("gRPC Server Shutdown complete");
        }
    }

    private Server createGrpcServer() {
        return ServerBuilder.forPort(appProperties.getGrpcPort())
                .executor(grpcExecutor)
                .addService(todoGrpcService)
                .intercept(errorHandlingInterceptor)
                .maxInboundMessageSize(MAX_INBOUND_MESSAGE_SIZE)
                .maxInboundMetadataSize(MAX_INBOUND_MESSAGE_SIZE)
                .build();
    }

}
