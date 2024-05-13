package dev.rgonzalez.demo.todo.api.domain;

import dev.rgonzalez.demo.todo.model.Priority;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TodoDTO(Integer id,
                      String description,
                      Priority priority,
                      LocalDate dueDate,
                      boolean completed,
                      LocalDateTime createdAt,
                      LocalDateTime completedAt) {
}
