package dev.rgonzalez.demo.todo.test.util;

import dev.rgonzalez.demo.todo.api.domain.TodoDTO;
import dev.rgonzalez.demo.todo.model.PageRequest;
import dev.rgonzalez.demo.todo.model.PagedResult;
import dev.rgonzalez.demo.todo.model.Priority;
import dev.rgonzalez.demo.todo.model.Todo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class TodoTestFactory {

    public static List<Todo> createTodoList() {
        return List.of(
                new Todo(1, "Task A", Priority.MEDIUM, LocalDate.of(2024, 5, 15),
                        false, LocalDateTime.now(), null),
                new Todo(2, "Task B", Priority.HIGH, LocalDate.of(2024, 5, 16),
                        true, LocalDateTime.now(), LocalDateTime.now()),
                new Todo(3, "Task C", Priority.LOW, null,
                        false, LocalDateTime.now(), null),
                new Todo(4, "Task D", Priority.MEDIUM, LocalDate.of(2024, 5, 17),
                        true, LocalDateTime.now(), LocalDateTime.now()),
                new Todo(5, "Task E", Priority.LOW, LocalDate.of(2024, 5, 18),
                        true, LocalDateTime.now(), LocalDateTime.now())
        );
    }

    public static PagedResult<Todo> createPagedResultOfTodos(int page, int size, int totalElements) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return new PagedResult<>(createTodoList(), totalElements, pageRequest);
    }

    public static List<TodoDTO> createTodoDTOList() {
        return List.of(
                new TodoDTO(1, "Task A", Priority.MEDIUM, LocalDate.of(2024, 5, 15),
                        false, LocalDateTime.now(), null),
                new TodoDTO(2, "Task B", Priority.HIGH, LocalDate.of(2024, 5, 16),
                        true, LocalDateTime.now(), LocalDateTime.now()),
                new TodoDTO(3, "Task C", Priority.LOW, null,
                        false, LocalDateTime.now(), null),
                new TodoDTO(4, "Task D", Priority.MEDIUM, LocalDate.of(2024, 5, 17),
                        true, LocalDateTime.now(), LocalDateTime.now()),
                new TodoDTO(5, "Task E", Priority.LOW, LocalDate.of(2024, 5, 18),
                        true, LocalDateTime.now(), LocalDateTime.now())
        );
    }

    public static PagedResult<TodoDTO> createPagedResultOfTodoDTOs(int page, int size, int totalElements) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return new PagedResult<>(createTodoDTOList(), totalElements, pageRequest);
    }

}
