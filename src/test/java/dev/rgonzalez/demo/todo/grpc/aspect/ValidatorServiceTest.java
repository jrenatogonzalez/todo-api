package dev.rgonzalez.demo.todo.grpc.aspect;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidatorServiceTest {

    private ValidatorService validatorService;

    @BeforeEach
    void setUp() {
        validatorService = new ValidatorService();
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenValidatingCreateTodoRequest() {
        // Given
        var request = CreateTodoRequestWrapper.builder().build();

        // When
        assertThatThrownBy(() -> validatorService.validate(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Description is required.");
    }

}