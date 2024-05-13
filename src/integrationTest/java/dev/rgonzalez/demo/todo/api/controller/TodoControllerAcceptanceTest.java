package dev.rgonzalez.demo.todo.api.controller;

import dev.rgonzalez.demo.todo.model.Priority;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql("/scripts/todo-controller-test-data.sql")
class TodoControllerAcceptanceTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldFindTheRequestedPageOfTodos() throws Exception {
        // Given
        int page = 1;
        int size = 5;

        // When & Then
        mockMvc.perform(get("/todos")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.totalElements").value(7))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    void shouldFindTheFirstPageOfTodos() throws Exception {
        // When & Then
        mockMvc.perform(get("/todos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.totalElements").value(7))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.content", hasSize(7)));
    }

    @Test
    void shouldFindTodoById() throws Exception {
        // Given
        Integer id = 5;

        // When & Then
        mockMvc.perform(get("/todos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.description").value("Task DDD"))
                .andExpect(jsonPath("$.priority").value(Priority.HIGH.name()))
                .andExpect(jsonPath("$.dueDate").value("2024-08-10"))
                .andExpect(jsonPath("$.completed").value(false));
    }

    @Test
    void shouldReturn404WhenFindingANonExistingTodo() throws Exception {
        // Given
        Integer id = 4;

        // When & Then
        mockMvc.perform(get("/todos/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    void shouldCreateTodo() throws Exception {
        // Given
        String description = "Todo to be created";
        String createTodoRequest = """
                { "description": "Todo to be created", "priority": "LOW", "dueDate": "2024-05-18" }
                """;

        // When & Then
        mockMvc.perform(post("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createTodoRequest)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("http://localhost/todos/")))
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.description").value(description))
                .andExpect(jsonPath("$.priority").value(Priority.LOW.name()))
                .andExpect(jsonPath("$.dueDate").value("2024-05-18"))
                .andExpect(jsonPath("$.completed").value(false));
    }

    @Test
    void shouldFailToCreateTodoWhenDescriptionIsEmpty() throws Exception {
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
    }

    @Test
    void shouldUpdateTodo() throws Exception {
        // Given
        Integer id = 3;
        String description = "Todo to be updated";
        String updateTodoRequest = """
                { "description": "Todo to be updated", "completed": true }
                """;

        // When & Then
        mockMvc.perform(patch("/todos/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateTodoRequest)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.description").value(description))
                .andExpect(jsonPath("$.priority").value(Priority.MEDIUM.name()))
                .andExpect(jsonPath("$.dueDate").isEmpty())
                .andExpect(jsonPath("$.completed").value(true))
                .andExpect(jsonPath("$.completedAt").isNotEmpty());
    }

    @Test
    void shouldReturn404WhenUpdatingANonExistingTodo() throws Exception {
        // Given
        Integer id = 4;
        String updateTodoRequest = """
                { "description": "Todo to be updated", "completed": true }
                """;

        // When & Then
        mockMvc.perform(patch("/todos/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateTodoRequest)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.message").value("Todo not found"));
    }

    @Test
    void shouldDeleteTodo() throws Exception {
        // Given
        Integer id = 7;

        // When & Then
        mockMvc.perform(delete("/todos/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(204))
                .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingTodo() throws Exception {
        // Given
        Integer id = 4;

        // When & Then
        mockMvc.perform(delete("/todos/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.message").value("Todo not found"));
    }

}
