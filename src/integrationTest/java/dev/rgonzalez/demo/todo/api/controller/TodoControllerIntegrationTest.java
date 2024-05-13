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
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TodoController.class)
class TodoControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TodoService todoService;

    @MockBean
    private TodoDTOConverter converter;

    @Test
    void shouldFindTheRequestedPageOfTodos() throws Exception {
        // Given
        int page = 3;
        int size = 5;
        int totalElements = 150;
        PagedResult<Todo> todoPagedResult = TodoTestFactory.createPagedResultOfTodos(page, size, totalElements);
        PagedResult<TodoDTO> todoDTOPagedResult = TodoTestFactory.createPagedResultOfTodoDTOs(page, size, totalElements);
        when(todoService.findAll(any(PageRequest.class))).thenReturn(todoPagedResult);
        when(converter.toDTO(todoPagedResult)).thenReturn(todoDTOPagedResult);

        // When
        mockMvc.perform(get("/todos")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.totalElements").value(150))
                .andExpect(jsonPath("$.totalPages").value(30))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.page").value(3))
                .andExpect(jsonPath("$.content", hasSize(5)));


        // Then
        ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(todoService).findAll(pageRequestCaptor.capture());
        PageRequest pageRequest = pageRequestCaptor.getValue();
        assertThat(pageRequest.getPage()).isEqualTo(page);
        assertThat(pageRequest.getSize()).isEqualTo(size);
        verify(converter).toDTO(todoPagedResult);
    }

    @Test
    void shouldFindTheFirstPageOfTodos() throws Exception {
        // Given
        int page = 0;
        int size = 20;
        int totalElements = 5;
        PagedResult<Todo> todoPagedResult = TodoTestFactory.createPagedResultOfTodos(page, size, totalElements);
        PagedResult<TodoDTO> todoDTOPagedResult = TodoTestFactory.createPagedResultOfTodoDTOs(page, size, totalElements);
        when(todoService.findAll(any(PageRequest.class))).thenReturn(todoPagedResult);
        when(converter.toDTO(todoPagedResult)).thenReturn(todoDTOPagedResult);

        // When
        mockMvc.perform(get("/todos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.content", hasSize(5)));

        // Then
        ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(todoService).findAll(pageRequestCaptor.capture());
        PageRequest pageRequest = pageRequestCaptor.getValue();
        assertThat(pageRequest.getPage()).isEqualTo(page);
        assertThat(pageRequest.getSize()).isEqualTo(size);
        verify(converter).toDTO(todoPagedResult);
    }

    @Test
    void shouldFindTodoById() throws Exception {
        // Given
        Integer id = 10;
        Todo todo = new Todo(10, "My Task ID 10", Priority.MEDIUM,
                LocalDate.of(2024, 5, 18), false, LocalDateTime.now(), null);
        TodoDTO todoDTO = new TodoDTO(10, "My Task ID 10", Priority.MEDIUM,
                LocalDate.of(2024, 5, 18), false, LocalDateTime.now(), null);
        when(todoService.findById(id)).thenReturn(Optional.of(todo));
        when(converter.toDTO(todo)).thenReturn(todoDTO);

        // When
        mockMvc.perform(get("/todos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.description").value("My Task ID 10"))
                .andExpect(jsonPath("$.priority").value(Priority.MEDIUM.name()))
                .andExpect(jsonPath("$.dueDate").value("2024-05-18"))
                .andExpect(jsonPath("$.completed").value(false));

        // Then
        verify(todoService).findById(id);
        verify(converter).toDTO(todo);
    }

    @Test
    void shouldReturn404WhenFindingANonExistingTodo() throws Exception {
        // Given
        Integer id = 12;
        when(todoService.findById(id)).thenReturn(Optional.empty());

        // When
        mockMvc.perform(get("/todos/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").doesNotExist());

        // Then
        verify(todoService).findById(id);
        verify(converter, never()).toDTO(any(Todo.class));
    }

    @Test
    void shouldCreateTodo() throws Exception {
        // Given
        Integer id = 25;
        String description = "Todo to be created";
        LocalDate dueDate = LocalDate.of(2024, 5, 18);
        LocalDateTime createdAt = LocalDateTime.now();
        String createTodoRequest = """
                { "description": "Todo to be created", "priority": "LOW", "dueDate": "2024-05-18" }
                """;
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

        // When
        mockMvc.perform(post("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createTodoRequest)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/todos/25"))
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.description").value(description))
                .andExpect(jsonPath("$.priority").value(Priority.LOW.name()))
                .andExpect(jsonPath("$.dueDate").value("2024-05-18"))
                .andExpect(jsonPath("$.completed").value(false));

        // Then
        verify(converter).toModel(request);
        verify(todoService).create(todoToBeCreated);
        verify(converter).toDTO(createdToDo);
    }

    @Test
    void shouldFailToCreateTodoWhenDescriptionIsEmpty() throws Exception {
        // Given
        String createTodoRequest = """
                {  "priority": "LOW", "dueDate": "2024-05-18" }
                """;

        // When & Then
        mockMvc.perform(post("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createTodoRequest)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("Description is required"));

        // Then
        verify(converter, never()).toModel(any(CreateTodoRequest.class));
        verify(todoService, never()).create(any(Todo.class));
        verify(converter, never()).toDTO(any(Todo.class));

    }


    @Test
    void shouldUpdateTodo() throws Exception {
        // Given
        Integer id = 15;
        String description = "Todo to be updated";
        LocalDate dueDate = LocalDate.of(2024, 5, 18);
        LocalDateTime createdAt = LocalDateTime.of(2024, 5, 10, 9, 0);
        LocalDateTime completedAt = LocalDateTime.now();
        String updateTodoRequest = """
                { "description": "Todo to be updated", "completed": true }
                """;
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
        mockMvc.perform(patch("/todos/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateTodoRequest)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.description").value(description))
                .andExpect(jsonPath("$.priority").value(Priority.MEDIUM.name()))
                .andExpect(jsonPath("$.dueDate").value("2024-05-18"))
                .andExpect(jsonPath("$.completed").value(true))
                .andExpect(jsonPath("$.completedAt").isNotEmpty());

        // Then
        verify(converter).toModel(id, request);
        verify(todoService).update(todoToBeUpdated);
        verify(converter).toDTO(updatedTodo);
    }

    @Test
    void shouldReturn404WhenUpdatingANonExistingTodo() throws Exception {
        // Given
        Integer id = 14;
        String description = "Todo to be updated";
        LocalDate dueDate = LocalDate.of(2024, 5, 18);
        LocalDateTime createdAt = LocalDateTime.of(2024, 5, 10, 9, 0);
        String updateTodoRequest = """
                { "description": "Todo to be updated", "completed": true }
                """;
        UpdateTodoRequest request = new UpdateTodoRequest(description, null, null, true);
        Todo todoToBeUpdated = new Todo(id, description, Priority.MEDIUM,
                dueDate, true, createdAt, null);

        when(converter.toModel(id, request)).thenReturn(todoToBeUpdated);
        doThrow(new NotFoundException("Todo not found")).when(todoService).update(todoToBeUpdated);

        // When
        mockMvc.perform(patch("/todos/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateTodoRequest)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.message").value("Todo not found"));

        // Then
        verify(todoService).update(todoToBeUpdated);
    }

    @Test
    void shouldDeleteTodo() throws Exception {
        // Given
        Integer id = 11;
        doNothing().when(todoService).deleteById(id);

        // When
        mockMvc.perform(delete("/todos/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(204))
                .andExpect(jsonPath("$").doesNotExist());

        // Then
        verify(todoService).deleteById(id);
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingTodo() throws Exception {
        // Given
        Integer id = 13;
        doThrow(new NotFoundException("Todo not found")).when(todoService).deleteById(id);

        // When
        mockMvc.perform(delete("/todos/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.message").value("Todo not found"));

        // Then
        verify(todoService).deleteById(id);
    }


}
