package dev.rgonzalez.demo.todo.grpc.converter;

import java.util.Optional;

public interface EnumModelConverter<M, P> {
    Optional<M> toModel(P proto);
}
