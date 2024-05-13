package dev.rgonzalez.demo.todo.api.converter;

import dev.rgonzalez.demo.todo.api.domain.CreateTodoRequest;
import dev.rgonzalez.demo.todo.api.domain.TodoDTO;
import dev.rgonzalez.demo.todo.api.domain.UpdateTodoRequest;
import dev.rgonzalez.demo.todo.model.PagedResult;
import dev.rgonzalez.demo.todo.model.Priority;
import dev.rgonzalez.demo.todo.model.Todo;
import dev.rgonzalez.demo.todo.test.util.TodoTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TodoDTOConverterTest {
    private TodoDTOConverter converter;

    @BeforeEach
    void setUp() {
        converter = new TodoDTOConverter();
    }

    @Test
    void shouldConvertToTodoDTO() {
        // Given
        Todo todo = Todo.builder()
                .id(10)
                .description("My First Task")
                .priority(Priority.HIGH)
                .dueDate(LocalDate.now())
                .completed(false)
                .createdAt(LocalDateTime.now())
                .build();

        // When
        TodoDTO todoDTO = converter.toDTO(todo);

        // Then
        assertThat(todoDTO).isNotNull();
        assertThat(todoDTO.id()).isEqualTo(todo.getId());
        assertThat(todoDTO.description()).isEqualTo(todo.getDescription());
        assertThat(todoDTO.priority()).isEqualTo(todo.getPriority());
        assertThat(todoDTO.dueDate()).isEqualTo(todo.getDueDate());
        assertThat(todoDTO.completed()).isEqualTo(todo.isCompleted());
        assertThat(todoDTO.createdAt()).isEqualTo(todo.getCreatedAt());
        assertThat(todoDTO.completedAt()).isNull();
    }

    @Test
    void shouldConvertToPagedResultOfDTO() {
        // Given
        int page = 1;
        int size = 5;
        int totalElements = 150;
        PagedResult<Todo> pagedResult = TodoTestFactory.createPagedResultOfTodos(page, size, totalElements);

        // When
        PagedResult<TodoDTO> result = converter.toDTO(pagedResult);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPage()).isEqualTo(pagedResult.getPage());
        assertThat(result.getSize()).isEqualTo(pagedResult.getSize());
        assertThat(result.getTotalPages()).isEqualTo(pagedResult.getTotalPages());
        assertThat(result.getTotalElements()).isEqualTo(pagedResult.getTotalElements());
        assertThat(result.getContent()).hasSameSizeAs(pagedResult.getContent());
    }

    @Test
    void shouldConvertToModelFromCreateTodoRequest() {
        // Given
        CreateTodoRequest request = new CreateTodoRequest("My Second Task", Priority.MEDIUM,
                LocalDate.of(2024, 5, 15));

        // When
        Todo todo = converter.toModel(request);

        // Then
        assertThat(todo).isNotNull();
        assertThat(todo.getDescription()).isEqualTo(request.description());
        assertThat(todo.getPriority()).isEqualTo(request.priority());
        assertThat(todo.getDueDate()).isEqualTo(request.dueDate());
        assertThat(todo.getCreatedAt()).isNull();
        assertThat(todo.isCompleted()).isFalse();
        assertThat(todo.getCompletedAt()).isNull();
    }

    @Test
    void shouldConvertToModelFromUpdateTodoRequest() {
        // Given
        Integer id = 11;
        UpdateTodoRequest request = new UpdateTodoRequest("Updating Task", Priority.LOW,
                LocalDate.of(2024, 5, 31), true);

        // When
        Todo todo = converter.toModel(id, request);

        // Then
        assertThat(todo).isNotNull();
        assertThat(todo.getDescription()).isEqualTo(request.description());
        assertThat(todo.getPriority()).isEqualTo(request.priority());
        assertThat(todo.getDueDate()).isEqualTo(request.dueDate());
        assertThat(todo.isCompleted()).isEqualTo(request.completed());
        assertThat(todo.getCreatedAt()).isNull();
        assertThat(todo.getCompletedAt()).isNull();
    }

}