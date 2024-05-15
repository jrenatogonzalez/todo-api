package dev.rgonzalez.demo.todo.api.controller;

import dev.rgonzalez.demo.todo.api.converter.TodoDTOConverter;
import dev.rgonzalez.demo.todo.api.domain.CreateTodoRequest;
import dev.rgonzalez.demo.todo.api.domain.TodoDTO;
import dev.rgonzalez.demo.todo.api.domain.UpdateTodoRequest;
import dev.rgonzalez.demo.todo.exceptions.NotFoundException;
import dev.rgonzalez.demo.todo.model.PageRequest;
import dev.rgonzalez.demo.todo.model.PagedResult;
import dev.rgonzalez.demo.todo.model.Priority;
import dev.rgonzalez.demo.todo.model.Todo;
import dev.rgonzalez.demo.todo.service.TodoService;
import dev.rgonzalez.demo.todo.test.util.TodoTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TodoControllerTest {
    @Mock
    private TodoService todoService;
    @Mock
    private TodoDTOConverter converter;

    @InjectMocks
    private TodoController todoController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldFindAPageOfTodos() {
        // Given
        int page = 2;
        int size = 5;
        int totalElements = 150;
        PagedResult<Todo> todoPagedResult = TodoTestFactory.createPagedResultOfTodos(page, size, totalElements);
        PagedResult<TodoDTO> todoDTOPagedResult = TodoTestFactory.createPagedResultOfTodoDTOs(page, size, totalElements);
        when(todoService.findAll(any(PageRequest.class)))
                .thenReturn(todoPagedResult);
        when(converter.toDTO(todoPagedResult))
                .thenReturn(todoDTOPagedResult);

        // When
        PagedResult<TodoDTO> pagedResult = todoController.findAll(page, size);

        // Then
        ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(todoService).findAll(pageRequestCaptor.capture());
        PageRequest pageRequest = pageRequestCaptor.getValue();
        assertThat(pageRequest.getPage()).isEqualTo(page);
        assertThat(pageRequest.getSize()).isEqualTo(size);
        verify(converter).toDTO(todoPagedResult);
        assertThat(pagedResult.getContent()).hasSameSizeAs(todoPagedResult.getContent());
    }

    @Test
    void shouldFindTodoById() {
        // Given
        Integer id = 10;
        Todo todo = new Todo(10, "My Task ID 10", Priority.MEDIUM,
                LocalDate.of(2024, 5, 18), false, LocalDateTime.now(), null);
        TodoDTO todoDTO = new TodoDTO(10, "My Task ID 10", Priority.MEDIUM,
                LocalDate.of(2024, 5, 18), false, LocalDateTime.now(), null);
        when(todoService.findById(id)).thenReturn(Optional.of(todo));
        when(converter.toDTO(todo)).thenReturn(todoDTO);

        // When
        ResponseEntity<TodoDTO> responseEntity = todoController.findById(id);

        // Then
        verify(todoService).findById(id);
        verify(converter).toDTO(todo);
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(responseEntity.getBody()).isEqualTo(todoDTO);
    }

    @Test
    void shouldReturnNotFoundStatusWhenFindingByNonExistingId() {
        // Given
        Integer id = 12;
        when(todoService.findById(id)).thenReturn(Optional.empty());

        // When
        ResponseEntity<TodoDTO> responseEntity = todoController.findById(id);

        // Then
        verify(todoService).findById(id);
        verify(converter, never()).toDTO(any(Todo.class));
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode().is4xxClientError()).isTrue();
        assertThat(responseEntity.getStatusCode().value()).isEqualTo(404);
        assertThat(responseEntity.getBody()).isNull();
    }

    @Test
    void shouldCreateTodo() {
        // Given
        Integer id = 25;
        String description = "Todo to be created";
        LocalDate dueDate = LocalDate.of(2024, 5, 18);
        LocalDateTime createdAt = LocalDateTime.now();
        CreateTodoRequest request = new CreateTodoRequest(description, Priority.LOW, dueDate);
        Todo todoToBeCreated = Todo.builder()
                .description(description)
                .priority(Priority.LOW)
                .dueDate(dueDate)
                .build();
        Todo createdToDo = Todo.builder()
                .id(id)
                .description(todoToBeCreated.getDescription())
                .priority(todoToBeCreated.getPriority())
                .dueDate(todoToBeCreated.getDueDate())
                .createdAt(createdAt)
                .build();
        TodoDTO todoDTO = new TodoDTO(id, description, Priority.LOW, dueDate, false, createdAt, null);
        when(converter.toModel(request)).thenReturn(todoToBeCreated);
        when(todoService.create(todoToBeCreated)).thenReturn(createdToDo);
        when(converter.toDTO(createdToDo)).thenReturn(todoDTO);
        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletRequest.setRequestURI("/todos/");
        RequestAttributes requestAttributes = new ServletRequestAttributes(mockHttpServletRequest);
        RequestContextHolder.setRequestAttributes(requestAttributes);

        // When
        ResponseEntity<TodoDTO> responseEntity = todoController.create(request);

        // Then
        verify(converter).toModel(request);
        verify(todoService).create(todoToBeCreated);
        verify(converter).toDTO(createdToDo);
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(responseEntity.getStatusCode().value()).isEqualTo(201);
        assertThat(responseEntity.getHeaders()).containsKey("Location");
        List<String> locations = responseEntity.getHeaders().get("Location");
        assertThat(locations).hasSize(1);
        assertThat(locations.getFirst()).isEqualTo("http://localhost/todos/25");
        assertThat(responseEntity.getBody()).isEqualTo(todoDTO);
    }

    @Test
    void shouldUpdateTodo() {
        // Given
        Integer id = 15;
        String description = "Todo to be updated";
        LocalDate dueDate = LocalDate.of(2024, 5, 18);
        LocalDateTime createdAt = LocalDateTime.of(2024, 5, 10, 9, 0);
        LocalDateTime completedAt = LocalDateTime.now();
        UpdateTodoRequest request = new UpdateTodoRequest(description, null, null, true);
        Todo todoToBeUpdated = new Todo(id, description, Priority.MEDIUM,
                dueDate, true, createdAt, null);
        Todo updatedTodo = new Todo(id, description, Priority.MEDIUM,
                dueDate, true, createdAt, completedAt);
        TodoDTO todoDTO = new TodoDTO(id, description, Priority.MEDIUM,
                dueDate, true, createdAt, completedAt);
        when(converter.toModel(id, request)).thenReturn(todoToBeUpdated);
        when(todoService.update(todoToBeUpdated)).thenReturn(updatedTodo);
        when(converter.toDTO(updatedTodo)).thenReturn(todoDTO);

        // When
        ResponseEntity<TodoDTO> responseEntity = todoController.update(id, request);

        // Then
        verify(converter).toModel(id, request);
        verify(todoService).update(todoToBeUpdated);
        verify(converter).toDTO(updatedTodo);
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(responseEntity.getBody()).isEqualTo(todoDTO);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUpdatingNonExistingTodo() {
        // Given
        Integer id = 14;
        String description = "Todo to be updated";
        LocalDateTime createdAt = LocalDateTime.of(2024, 5, 10, 9, 0);
        UpdateTodoRequest request = new UpdateTodoRequest(description, null, null, true);
        Todo todoToBeUpdated = new Todo(id, description, Priority.MEDIUM,
                LocalDate.of(2024, 5, 18), true, createdAt, null);

        when(converter.toModel(id, request)).thenReturn(todoToBeUpdated);
        doThrow(new NotFoundException("Todo not found")).when(todoService).update(todoToBeUpdated);

        // When
        assertThatThrownBy(() -> todoController.update(id, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Todo not found");

        // Then
        verify(todoService).update(todoToBeUpdated);
    }


    @Test
    void shouldDeleteTodo() {
        // Given
        Integer id = 11;
        doNothing().when(todoService).deleteById(id);

        // When
        ResponseEntity<Void> responseEntity = todoController.delete(id);

        // Then
        verify(todoService).deleteById(id);
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(responseEntity.getBody()).isNull();
    }

    @Test
    void shouldThrowNotFoundExceptionWhenDeletingNonExistingTodo() {
        // Given
        Integer id = 13;
        doThrow(new NotFoundException("Todo not found")).when(todoService).deleteById(id);

        // When
        assertThatThrownBy(() -> todoController.delete(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Todo not found");

        // Then
        verify(todoService).deleteById(id);
    }
}