package dev.rgonzalez.demo.todo.grpc.converter;

import dev.rgonzalez.proto.todo.client.todo.CreateTodoRequest;
import dev.rgonzalez.proto.todo.common.messages.Date;
import dev.rgonzalez.proto.todo.common.messages.Priority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Optional;

import static dev.rgonzalez.demo.todo.model.Priority.LOW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class CreateTodoRequestProtoConverterTest {
    @Mock
    private PriorityProtoConverter priorityProtoConverter;

    @Mock
    private DateProtoConverter dateProtoConverter;

    @InjectMocks
    private CreateTodoRequestProtoConverter converter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnOptionalEmptyWhenCreateTotoRequestIsNull() {
        // When
        var result = converter.toModel(null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldConvertToTodo() {
        // Given
        Date date = Date.newBuilder()
                .setYear(2024)
                .setMonth(5)
                .setDay(30)
                .build();
        CreateTodoRequest request = CreateTodoRequest.newBuilder()
                .setDescription("A new Todo")
                .setPriority(Priority.PRIORITY_LOW)
                .setDueDate(date)
                .build();
        when(priorityProtoConverter.toModel(Priority.PRIORITY_LOW))
                .thenReturn(Optional.of(LOW));
        when(dateProtoConverter.toModel(date))
                .thenReturn(Optional.of(LocalDate.of(2024,5,30)));

        // When
        var response = converter.toModel(request);

        // Then
        assertThat(response).isPresent();
        var todo = response.get();
        assertThat(todo.getDescription()).isEqualTo("A new Todo");
        assertThat(todo.getPriority()).isEqualTo(LOW);
        assertThat(todo.getDueDate().getYear()).isEqualTo(2024);
        assertThat(todo.getDueDate().getMonthValue()).isEqualTo(5);
        assertThat(todo.getDueDate().getDayOfMonth()).isEqualTo(30);
    }

}