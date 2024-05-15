package dev.rgonzalez.demo.todo.grpc.aspect;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CreateTodoRequestWrapper {
    @NotEmpty(message = "Description is required.")
    private String description;

}
