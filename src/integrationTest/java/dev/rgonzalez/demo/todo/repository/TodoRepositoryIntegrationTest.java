package dev.rgonzalez.demo.todo.repository;

import dev.rgonzalez.demo.todo.exceptions.NotFoundException;
import dev.rgonzalez.demo.todo.model.PageRequest;
import dev.rgonzalez.demo.todo.model.PagedResult;
import dev.rgonzalez.demo.todo.model.Priority;
import dev.rgonzalez.demo.todo.model.Todo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest(properties = {"spring.test.database.replace=none"})
@Sql("/scripts/todo-repository-test-data.sql")
class TodoRepositoryIntegrationTest {
    @Autowired
    private JdbcClient jdbcClient;
    private TodoRepository todoRepository;

    @BeforeEach
    void setup() {
        todoRepository = new TodoRepository(jdbcClient);
    }

    @Test
    void shouldCreateNewTodo() {
        // Given
        Todo todo = Todo.builder()
                .description("Drink coffee")
                .priority(Priority.LOW)
                .dueDate(LocalDate.now())
                .build();

        // When
        Todo createdTodo = todoRepository.create(todo);

        // Then
        assertThat(createdTodo).isNotNull();
        assertThat(createdTodo.getId()).isNotNull();
        assertThat(createdTodo.getDescription()).isEqualTo(todo.getDescription());
        assertThat(createdTodo.getPriority()).isEqualTo(todo.getPriority());
        assertThat(createdTodo.getDueDate()).isEqualTo(todo.getDueDate());
        assertThat(createdTodo.getCreatedAt()).isNotNull();
        assertThat(createdTodo.getCompletedAt()).isNull();
        assertThat(createdTodo.isCompleted()).isFalse();
    }

    @Test
    void shouldFindTodoById() {
        // Given
        Integer id = 6;

        // When
        Optional<Todo> result = todoRepository.findById(id);

        // Then
        assertThat(result).isPresent();
        Todo todo = result.get();
        assertThat(todo.getId()).isEqualTo(6);
        assertThat(todo.getDescription()).isEqualTo("Task EEE");
        assertThat(todo.getPriority()).isEqualTo(Priority.MEDIUM);
        assertThat(todo.getDueDate()).isNull();
        assertThat(todo.isCompleted()).isFalse();
        assertThat(todo.getCompletedAt()).isNull();
        assertThat(todo.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldGetEmptyOptionalWhenTodoNotFound() {
        // Given
        Integer id = 4;

        // When
        Optional<Todo> result = todoRepository.findById(id);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldUpdateTodoWhenIsCompleted() {
        // Given
        Integer id = 6;
        Todo todoToBeUpdated = Todo.builder()
                .id(id)
                .completed(true)
                .build();

        // When
        Todo updatedTodo = todoRepository.update(todoToBeUpdated);

        // Then
        assertThat(updatedTodo).isNotNull();
        assertThat(updatedTodo.getId()).isEqualTo(id);
        assertThat(updatedTodo.getPriority()).isEqualTo(Priority.MEDIUM);
        assertThat(updatedTodo.getDueDate()).isNull();
        assertThat(updatedTodo.isCompleted()).isTrue();
        assertThat(updatedTodo.getCreatedAt()).isNotNull();
        assertThat(updatedTodo.getCompletedAt()).isNotNull();
        assertThat(updatedTodo.getDescription()).isEqualTo("Task EEE");
    }

    @Test
    void shouldUpdateTodoToUncompleted() {
        // Given
        Integer id = 20;
        Todo todoToBeUpdated = Todo.builder()
                .id(id)
                .description("An uncompleted task")
                .completed(false)
                .build();

        // When
        Todo updatedTodo = todoRepository.update(todoToBeUpdated);

        // Then
        assertThat(updatedTodo).isNotNull();
        assertThat(updatedTodo.getId()).isEqualTo(id);
        assertThat(updatedTodo.isCompleted()).isFalse();
        assertThat(updatedTodo.getPriority()).isEqualTo(Priority.MEDIUM);
        assertThat(updatedTodo.getDueDate()).isEqualTo(LocalDate.of(2024, 5, 15));
        assertThat(updatedTodo.getCompletedAt()).isNull();
        assertThat(updatedTodo.getDescription()).isEqualTo(todoToBeUpdated.getDescription());
        assertThat(updatedTodo.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldUpdatePriority() {
        Integer id = 5;
        Todo todoToBeUpdated = Todo.builder()
                .id(id)
                .priority(Priority.MEDIUM)
                .build();

        // When
        Todo updatedTodo = todoRepository.update(todoToBeUpdated);

        // Then
        assertThat(updatedTodo).isNotNull();
        assertThat(updatedTodo.getId()).isEqualTo(id);
        assertThat(updatedTodo.getPriority()).isEqualTo(Priority.MEDIUM);
        assertThat(updatedTodo.getDueDate()).isEqualTo(LocalDate.of(2024, 8, 10));
        assertThat(updatedTodo.isCompleted()).isFalse();
        assertThat(updatedTodo.getCompletedAt()).isNull();
        assertThat(updatedTodo.getDescription()).isEqualTo("Task DDD");
        assertThat(updatedTodo.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldUpdateDueDate() {
        Integer id = 3;
        LocalDate dueDate = LocalDate.of(2024, 6, 12);
        Todo todoToBeUpdated = Todo.builder()
                .id(id)
                .dueDate(dueDate)
                .build();

        // When
        Todo updatedTodo = todoRepository.update(todoToBeUpdated);

        // Then
        assertThat(updatedTodo).isNotNull();
        assertThat(updatedTodo.getId()).isEqualTo(id);
        assertThat(updatedTodo.getPriority()).isEqualTo(Priority.MEDIUM);
        assertThat(updatedTodo.isCompleted()).isFalse();
        assertThat(updatedTodo.getCompletedAt()).isNull();
        assertThat(updatedTodo.getDescription()).isEqualTo("Task CCC");
        assertThat(updatedTodo.getDueDate()).isEqualTo(dueDate);
        assertThat(updatedTodo.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldThrowNotFoundExceptionWhenTryingToUpdateNonExistingTodo() {
        // Given
        Integer id = 4;
        Todo todoToBeUpdated = Todo.builder()
                .id(id)
                .description("Updating non existing Todo")
                .build();

        // When & Then
        assertThatThrownBy(() -> todoRepository.update(todoToBeUpdated))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Todo not found");
    }

    @Test
    void shouldDeleteTodoById() {
        // Given
        Integer id = 3;
        Optional<Todo> toBeDeleted = todoRepository.findById(id);

        // When
        todoRepository.deleteById(id);

        // Then
        Optional<Todo> optionalTodo = todoRepository.findById(id);
        assertThat(toBeDeleted).isPresent();
        assertThat(optionalTodo).isNotPresent();
    }

    @Test
    void shouldThrowNotFoundExceptionWhenTryingToDeleteNonExistingTodo() {
        // Given
        Integer id = 4;

        // When & Then
        assertThatThrownBy(() -> todoRepository.deleteById(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Todo not found");
    }

    @Test
    void shouldReturnTotalTodoCount() {
        // When
        int count = todoRepository.count();

        // Then
        assertThat(count).isEqualTo(7);
    }

    @Test
    void shouldReturnFirstPage() {
        // Given
        PageRequest pageRequest = PageRequest.of(0, 5);

        // Then
        PagedResult<Todo> page = todoRepository.findAll(pageRequest);

        // When
        assertThat(page).isNotNull();
        List<Todo> content = page.getContent();
        assertThat(content).isNotNull().hasSize(5);
        assertThat(page.getPage()).isZero();
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getSize()).isEqualTo(5);
        assertThat(page.getTotalElements()).isEqualTo(7);
    }

    @Test
    void shouldReturnSecondPage() {
        // Given
        PageRequest pageRequest = PageRequest.of(1, 5);

        // Then
        PagedResult<Todo> page = todoRepository.findAll(pageRequest);

        // When
        assertThat(page).isNotNull();
        List<Todo> content = page.getContent();
        assertThat(content).isNotNull().hasSize(2);
        assertThat(page.getPage()).isEqualTo(1);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getSize()).isEqualTo(5);
        assertThat(page.getTotalElements()).isEqualTo(7);
    }

}
