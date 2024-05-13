package dev.rgonzalez.demo.todo.service;

import dev.rgonzalez.demo.todo.model.PageRequest;
import dev.rgonzalez.demo.todo.model.PagedResult;
import dev.rgonzalez.demo.todo.model.Priority;
import dev.rgonzalez.demo.todo.model.Todo;
import dev.rgonzalez.demo.todo.repository.TodoRepository;
import dev.rgonzalez.demo.todo.test.util.TodoTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultTodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @InjectMocks
    private DefaultTodoService todoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldFetchAPageOfTodos() {
        // Given
        PageRequest pageRequest = PageRequest.of(1, 5);
        List<Todo> todoList = TodoTestFactory.createTodoList();
        PagedResult<Todo> pagedResult = new PagedResult<>(todoList, 50, pageRequest);
        when(todoRepository.findAll(any(PageRequest.class)))
                .thenReturn(pagedResult);

        // When
        PagedResult<Todo> result = todoService.findAll(pageRequest);

        // Then
        assertThat(result).isNotNull();
        verify(todoRepository).findAll(pageRequest);
        assertThat(result.getContent()).hasSameSizeAs(pagedResult.getContent());
    }

    @Test
    void shouldFetchTodoById() {
        // Given
        Integer id = 10;
        Todo todo = new Todo(id, "A task", Priority.MEDIUM, LocalDate.of(2024, 5, 17),
                false, LocalDateTime.now(), null);
        when(todoRepository.findById(id)).thenReturn(Optional.of(todo));

        // When
        Optional<Todo> optionalTodo = todoService.findById(id);

        // Then
        assertThat(optionalTodo).isPresent();
        verify(todoRepository).findById(id);
        Todo fetchedTodo = optionalTodo.get();
        assertThat(fetchedTodo.getId()).isEqualTo(todo.getId());
        assertThat(fetchedTodo).isEqualTo(todo);
    }

    @Test
    void shouldCreateANewTodo() {
        // Given
        Todo todo = Todo.builder()
                .description("A new Task")
                .priority(Priority.HIGH)
                .dueDate(LocalDate.now())
                .build();
        Todo expectedCreatedTodo = new Todo(5, todo.getDescription(), todo.getPriority(), todo.getDueDate(),
                false, LocalDateTime.now(), null);
        when(todoRepository.create(todo)).thenReturn(expectedCreatedTodo);

        // When
        Todo createdTodo = todoService.create(todo);

        // Then
        assertThat(createdTodo)
                .isNotNull()
                .isEqualTo(expectedCreatedTodo);
        verify(todoRepository).create(todo);
    }

    @Test
    void shouldUpdateAnExistingTodo() {
        // Given
        Integer id = 12;
        Todo todo = Todo.builder()
                .description("A modified Task description")
                .completed(true)
                .build();

        Todo expectedUpdatedTodo = new Todo(id, todo.getDescription(), Priority.MEDIUM, LocalDate.now(),
                todo.isCompleted(), LocalDateTime.now(), LocalDateTime.now());
        when(todoRepository.update(todo)).thenReturn(expectedUpdatedTodo);

        // When
        Todo updatedTodo = todoService.update(todo);

        // Then
        assertThat(updatedTodo).isNotNull();
        assertThat(updatedTodo.getId()).isEqualTo(id);
        assertThat(updatedTodo.getDescription()).isEqualTo(todo.getDescription());
        assertThat(updatedTodo.isCompleted()).isEqualTo(todo.isCompleted());
        verify(todoRepository).update(todo);
    }

    @Test
    void shouldDeleteAnExistingTodo() {
        // Given
        Integer id = 14;

        // When
        todoService.deleteById(id);

        // Then
        verify(todoRepository).deleteById(id);
    }

}