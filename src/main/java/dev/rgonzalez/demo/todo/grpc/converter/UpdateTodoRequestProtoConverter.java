package dev.rgonzalez.demo.todo.grpc.converter;

import dev.rgonzalez.demo.todo.model.Todo;
import dev.rgonzalez.proto.todo.client.todo.UpdateTodoRequest;
import dev.rgonzalez.proto.todo.common.messages.Priority;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UpdateTodoRequestProtoConverter implements ModelConverter<Todo, UpdateTodoRequest> {
    private final PriorityProtoConverter priorityProtoConverter;
    private final DateProtoConverter dateProtoConverter;

    @Override
    public Optional<Todo> toModel(UpdateTodoRequest proto) {
        if (Objects.isNull(proto)) {
            return Optional.empty();
        }
        Todo.TodoBuilder builder = Todo.builder().id(proto.getId());

        if (proto.hasDescription()) {
            builder.description(proto.getDescription());
        }

        if (proto.hasDueDate()) {
            var optionalDueDate = dateProtoConverter.toModel(proto.getDueDate());
            optionalDueDate.ifPresent(builder::dueDate);
        }

        if (proto.hasPriority() && !proto.getPriority().equals(Priority.PRIORITY_UNSPECIFIED)) {
            var optionalPriority = priorityProtoConverter.toModel(proto.getPriority());
            optionalPriority.ifPresent(builder::priority);
        }

        if (proto.hasCompleted()) {
            builder.completed(proto.getCompleted());
        }

        return Optional.of(builder.build());
    }
}