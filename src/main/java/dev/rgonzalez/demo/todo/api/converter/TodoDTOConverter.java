package dev.rgonzalez.demo.todo.api.converter;

import dev.rgonzalez.demo.todo.api.domain.CreateTodoRequest;
import dev.rgonzalez.demo.todo.api.domain.TodoDTO;
import dev.rgonzalez.demo.todo.api.domain.UpdateTodoRequest;
import dev.rgonzalez.demo.todo.model.PageRequest;
import dev.rgonzalez.demo.todo.model.PagedResult;
import dev.rgonzalez.demo.todo.model.Todo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class TodoDTOConverter {

    public TodoDTO toDTO(Todo todo) {
        return new TodoDTO(todo.getId(),
                todo.getDescription(),
                todo.getPriority(),
                todo.getDueDate(),
                todo.isCompleted(),
                todo.getCreatedAt(),
                todo.getCompletedAt());
    }

    public PagedResult<TodoDTO> toDTO(PagedResult<Todo> pagedResult) {
        List<TodoDTO> content = pagedResult.getContent()
                .stream()
                .map(this::toDTO)
                .toList();
        return new PagedResult<>(
                content,
                pagedResult.getTotalElements(),
                PageRequest.of(pagedResult.getPage(), pagedResult.getSize())
        );
    }

    public Todo toModel(CreateTodoRequest createTodoRequest) {
        return Todo.builder()
                .description(createTodoRequest.description())
                .priority(createTodoRequest.priority())
                .dueDate(createTodoRequest.dueDate())
                .build();
    }

    public Todo toModel(Integer id, UpdateTodoRequest updateTodoRequest) {
        Todo.TodoBuilder builder = Todo.builder().id(id);

        Optional.ofNullable(updateTodoRequest.description())
                .ifPresent(builder::description);
        Optional.ofNullable(updateTodoRequest.priority())
                .ifPresent(builder::priority);
        Optional.ofNullable(updateTodoRequest.completed())
                .ifPresent(builder::completed);
        Optional.ofNullable(updateTodoRequest.dueDate())
                .ifPresent(builder::dueDate);

        return builder.build();
    }

}
