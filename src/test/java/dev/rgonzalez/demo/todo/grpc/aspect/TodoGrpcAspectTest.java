package dev.rgonzalez.demo.todo.grpc.aspect;

import dev.rgonzalez.proto.todo.client.todo.CreateTodoRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

class TodoGrpcAspectTest {
    @Mock
    private ValidatorService validatorService;
    @InjectMocks
    private TodoGrpcAspect todoGrpcAspect;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldThrowExceptionWhenDescriptionIsEmpty() {
        // Given
        var request = CreateTodoRequest.newBuilder()
                .build();
        doThrow(new IllegalArgumentException("Description is required."))
                .when(validatorService).validate(any());

        // When & Then
        assertThatThrownBy(() -> todoGrpcAspect.beforeCreateTodoRequest(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Description is required.");
    }

}