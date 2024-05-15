package dev.rgonzalez.demo.todo.grpc.converter;

import dev.rgonzalez.demo.todo.model.Todo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;


@Component
@RequiredArgsConstructor
public class TodoProtoConverter implements
        ProtoConverter<dev.rgonzalez.proto.todo.common.messages.Todo, Todo> {
    private final PriorityProtoConverter priorityProtoConverter;
    private final DateProtoConverter dateProtoConverter;
    private final LocalDateTimeProtoConverter localDateTimeProtoConverter;

    public Optional<dev.rgonzalez.proto.todo.common.messages.Todo> toProto(Todo todo) {
        if (Objects.isNull(todo)) {
            return Optional.empty();
        }
        dev.rgonzalez.proto.todo.common.messages.Todo.Builder builder = dev.rgonzalez.proto.todo.common.messages.Todo.newBuilder()
                .setId(todo.getId())
                .setDescription(todo.getDescription())
                .setCompleted(todo.isCompleted());

        Optional.ofNullable(todo.getPriority())
                .ifPresent(priority -> builder.setPriority(priorityProtoConverter.toProto(priority)));
        Optional.ofNullable(todo.getDueDate())
                .ifPresent(dueDate -> builder.setDueDate(dateProtoConverter.toProto(dueDate).get()));
        Optional.ofNullable(todo.getCompletedAt())
                .ifPresent(completedAt -> builder.setCompletedAt(localDateTimeProtoConverter.toProto(completedAt).get()));
        Optional.ofNullable(todo.getCreatedAt())
                .ifPresent(createdAt -> builder.setCreatedAt(localDateTimeProtoConverter.toProto(createdAt).get()));

        return Optional.of(builder.build());
    }

}
