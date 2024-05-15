package dev.rgonzalez.demo.todo.grpc.converter;

import com.google.protobuf.Timestamp;
import dev.rgonzalez.demo.todo.model.Priority;
import dev.rgonzalez.demo.todo.model.Todo;
import dev.rgonzalez.proto.todo.common.messages.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static dev.rgonzalez.proto.todo.common.messages.Priority.PRIORITY_HIGH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class TodoProtoConverterTest {
    @Mock
    private PriorityProtoConverter priorityProtoConverter;

    @Mock
    private DateProtoConverter dateProtoConverter;

    @Mock
    private LocalDateTimeProtoConverter localDateTimeProtoConverter;

    @InjectMocks
    private TodoProtoConverter todoProtoConverter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnOptionalEmptyWhenTodoIsNull() {
        // When
        var result = todoProtoConverter.toProto(null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldConvertToModelTodo() {
        // Given
        Todo todo = Todo.builder()
                .id(10)
                .description("My First Task")
                .priority(Priority.HIGH)
                .dueDate(LocalDate.of(2024, 6, 7))
                .completed(false)
                .createdAt(LocalDateTime.now())
                .build();
        Date expectedDueDate = Date.newBuilder()
                .setYear(2024)
                .setMonth(6)
                .setDay(7)
                .build();
        when(priorityProtoConverter.toProto(Priority.HIGH))
                .thenReturn(PRIORITY_HIGH);
        when(dateProtoConverter.toProto(LocalDate.of(2024, 6, 7)))
                .thenReturn(Optional.of(expectedDueDate));
        when(localDateTimeProtoConverter.toProto(any(LocalDateTime.class)))
                .thenReturn(Optional.of(Timestamp.newBuilder().build()));

        // When
        var result = todoProtoConverter.toProto(todo);

        // Then
        assertThat(result).isPresent();
        var protoTodo = result.get();
        assertThat(protoTodo.getId()).isEqualTo(todo.getId());
        assertThat(protoTodo.getDescription()).isEqualTo(todo.getDescription());
        assertThat(protoTodo.getPriority()).isEqualTo(PRIORITY_HIGH);
        Date dueDate = protoTodo.getDueDate();
        assertThat(dueDate.getYear()).isEqualTo(2024);
        assertThat(dueDate.getMonth()).isEqualTo(6);
        assertThat(dueDate.getDay()).isEqualTo(7);
        assertThat(protoTodo.getCompleted()).isEqualTo(todo.isCompleted());
        assertThat(protoTodo.hasCreatedAt()).isTrue();
        assertThat(protoTodo.hasCompletedAt()).isFalse();
    }

}