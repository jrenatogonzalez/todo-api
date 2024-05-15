package dev.rgonzalez.demo.todo.grpc.converter;

import dev.rgonzalez.demo.todo.model.Todo;
import dev.rgonzalez.proto.todo.client.todo.CreateTodoRequest;
import dev.rgonzalez.proto.todo.common.messages.Priority;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CreateTodoRequestProtoConverter implements ModelConverter<Todo, CreateTodoRequest> {
    private final PriorityProtoConverter priorityProtoConverter;
    private final DateProtoConverter dateProtoConverter;

    @Override
    public Optional<Todo> toModel(CreateTodoRequest proto) {
        if (Objects.isNull(proto)) {
            return Optional.empty();
        }
        Todo.TodoBuilder builder = Todo.builder().description(proto.getDescription());

        if (proto.hasPriority() && !proto.getPriority().equals(Priority.PRIORITY_UNSPECIFIED)) {
            var optionalPriority = priorityProtoConverter.toModel(proto.getPriority());
            optionalPriority.ifPresent(builder::priority);
        }
        if (proto.hasDueDate()) {
            var optionalDueDate = dateProtoConverter.toModel(proto.getDueDate());
            optionalDueDate.ifPresent(builder::dueDate);
        }
        return Optional.of(builder.build());
    }
}
