package dev.rgonzalez.demo.todo.grpc.converter;

import java.util.Optional;

public interface ModelConverter<M, P> {
    Optional<M> toModel(P proto);
}
