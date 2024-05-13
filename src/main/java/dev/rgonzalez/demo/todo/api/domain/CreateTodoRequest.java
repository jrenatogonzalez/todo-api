package dev.rgonzalez.demo.todo.api.domain;

import dev.rgonzalez.demo.todo.model.Priority;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDate;

public record CreateTodoRequest(@NotEmpty(message = "Description is required") String description,
                                Priority priority,
                                LocalDate dueDate) {
}
