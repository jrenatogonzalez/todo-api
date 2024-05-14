package dev.rgonzalez.demo.todo.grpc.converter;


public interface EnumProtoConverter<P, M> {
   P toProto(M model);
}
