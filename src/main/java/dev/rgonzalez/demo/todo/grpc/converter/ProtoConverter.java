package dev.rgonzalez.demo.todo.grpc.converter;

import java.util.Optional;

public interface ProtoConverter<P, M> {
    Optional<P> toProto(M model);
}
