package dev.rgonzalez.demo.todo.grpc.converter;

import dev.rgonzalez.demo.todo.model.Priority;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
public class PriorityProtoConverter implements
        EnumProtoConverter<dev.rgonzalez.proto.todo.common.messages.Priority, Priority>,
        EnumModelConverter<Priority, dev.rgonzalez.proto.todo.common.messages.Priority> {

    @Override
    public dev.rgonzalez.proto.todo.common.messages.Priority toProto(Priority priorityModel) {
        if (Objects.isNull(priorityModel)) {
            return dev.rgonzalez.proto.todo.common.messages.Priority.PRIORITY_UNSPECIFIED;
        }
        return dev.rgonzalez.proto.todo.common.messages.Priority.forNumber(priorityModel.ordinal() + 1);
    }

    @Override
    public Optional<Priority> toModel(dev.rgonzalez.proto.todo.common.messages.Priority priorityProto) {
        if (priorityProto.equals(dev.rgonzalez.proto.todo.common.messages.Priority.PRIORITY_UNSPECIFIED)) {
            return Optional.empty();
        }
        return Optional.of(Priority.values()[priorityProto.getNumber() - 1]);
    }

}
