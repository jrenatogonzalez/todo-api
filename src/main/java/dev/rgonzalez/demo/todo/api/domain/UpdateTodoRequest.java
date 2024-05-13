package dev.rgonzalez.demo.todo.api.domain;

import dev.rgonzalez.demo.todo.model.Priority;

import java.time.LocalDate;

public record UpdateTodoRequest(String description, Priority priority, LocalDate dueDate, Boolean completed) {
}
