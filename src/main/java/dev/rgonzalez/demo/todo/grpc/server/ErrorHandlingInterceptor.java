package dev.rgonzalez.demo.todo.grpc.server;

import dev.rgonzalez.demo.todo.exceptions.NotFoundException;
import io.grpc.ForwardingServerCall;
import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ErrorHandlingInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        ServerCall.Listener<ReqT> listener = next.startCall(new ForwardingServerCall.SimpleForwardingServerCall<>(call) {
            @Override
            public void close(Status status, Metadata trailers) {
                if (!status.isOk()) {
                    log.error("Error occurred: {}", status);
                }
                super.close(status, trailers);
            }
        }, headers);

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(listener) {
            @Override
            public void onHalfClose() {
                try {
                    super.onHalfClose();
                } catch (Exception e) {
                    Status status = getStatusFor(e);
                    call.close(status, new Metadata());
                }
            }

            @Override
            public void onMessage(ReqT message) {
                try {
                    super.onMessage(message);
                } catch (Exception e) {
                    Status status = Status.INTERNAL.withDescription(e.getMessage()).withCause(e);
                    call.close(status, new Metadata());
                }
            }

            private Status getStatusFor(Throwable throwable) {
                return switch (throwable) {
                    case IllegalArgumentException e -> Status.INVALID_ARGUMENT
                            .withDescription(e.getMessage())
                            .withCause(e.getCause());
                    case NotFoundException e -> Status.NOT_FOUND
                            .withDescription(e.getMessage())
                            .withCause(e.getCause());
                    default -> Status.INTERNAL
                            .withDescription(throwable.getMessage())
                            .withCause(throwable.getCause());
                };
            }
        };
    }

}
