package dev.rgonzalez.demo.todo.grpc.converter;

import dev.rgonzalez.demo.todo.model.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PageRequestProtoConverter implements
        ModelConverter<PageRequest, dev.rgonzalez.proto.todo.common.messages.PageRequest>,
        ProtoConverter<dev.rgonzalez.proto.todo.common.messages.PageRequest, PageRequest> {

    @Override
    public Optional<PageRequest> toModel(dev.rgonzalez.proto.todo.common.messages.PageRequest pageRequestProto) {
        return Optional.of(
                PageRequest.of(pageRequestProto.getPage(), pageRequestProto.getSize())
        );
    }

    @Override
    public Optional<dev.rgonzalez.proto.todo.common.messages.PageRequest> toProto(PageRequest pageRequestModel) {
        return Optional.of(
                dev.rgonzalez.proto.todo.common.messages.PageRequest.newBuilder()
                        .setPage(pageRequestModel.getPage())
                        .setSize(pageRequestModel.getSize())
                        .build()
        );
    }

}
