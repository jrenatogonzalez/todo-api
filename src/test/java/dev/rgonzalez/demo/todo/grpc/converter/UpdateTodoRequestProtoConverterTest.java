package dev.rgonzalez.demo.todo.grpc.converter;

import dev.rgonzalez.proto.todo.client.todo.UpdateTodoRequest;
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

class UpdateTodoRequestProtoConverterTest {
    @Mock
    private PriorityProtoConverter priorityProtoConverter;

    @Mock
    private DateProtoConverter dateProtoConverter;

    @InjectMocks
    private UpdateTodoRequestProtoConverter updateTodoRequestProtoConverter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnOptionalEmptyWhenUpdateTodoRequestIsNull() {
        // When
        var result = updateTodoRequestProtoConverter.toModel(null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldConvertToTodo() {
        // Given
        int id = 11;
        Date date = Date.newBuilder()
                .setYear(2024)
                .setMonth(5)
                .setDay(30)
                .build();
        UpdateTodoRequest request = UpdateTodoRequest.newBuilder()
                .setId(id)
                .setDescription("Updating a Todo")
                .setPriority(Priority.PRIORITY_LOW)
                .setDueDate(date)
                .setCompleted(true)
                .build();
        when(priorityProtoConverter.toModel(Priority.PRIORITY_LOW))
                .thenReturn(Optional.of(LOW));
        when(dateProtoConverter.toModel(date))
                .thenReturn(Optional.of(LocalDate.of(2024, 5, 30)));

        // When
        var response = updateTodoRequestProtoConverter.toModel(request);

        // Then
        assertThat(response).isPresent();
        var todo = response.get();
        assertThat(todo.getDescription()).isEqualTo("Updating a Todo");
        assertThat(todo.getPriority()).isEqualTo(LOW);
        assertThat(todo.getDueDate().getYear()).isEqualTo(2024);
        assertThat(todo.getDueDate().getMonthValue()).isEqualTo(5);
        assertThat(todo.getDueDate().getDayOfMonth()).isEqualTo(30);
        assertThat(todo.isCompleted()).isTrue();
        assertThat(todo.getCreatedAt()).isNull();
        assertThat(todo.getCompletedAt()).isNull();

    }

}