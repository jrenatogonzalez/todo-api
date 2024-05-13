package dev.rgonzalez.demo.todo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Getter
@Setter
@Builder
@AllArgsConstructor
public class Todo {
    private Integer id;
    private String description;
    private Priority priority;
    private LocalDate dueDate;
    private boolean completed;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

}
