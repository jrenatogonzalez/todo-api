package dev.rgonzalez.demo.todo.grpc.converter;

import dev.rgonzalez.demo.todo.model.PagedResult;
import dev.rgonzalez.demo.todo.model.Todo;
import dev.rgonzalez.demo.todo.test.util.TodoTestFactory;
import dev.rgonzalez.proto.todo.client.todo.ListTodoResponse;
import dev.rgonzalez.proto.todo.common.messages.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class PagedResultProtoConverterTest {
    @Mock
    private TodoProtoConverter todoProtoConverter;
    @InjectMocks
    private PagedResultProtoConverter pagedResultProtoConverter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnEmptyOptionalWhenModelPagedResultIsNull() {
        // When
        var result = pagedResultProtoConverter.toProto(null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldConvertToListTodoResponse() {
        // Given
        int page = 1;
        int size = 5;
        int totalElements = 150;
        PagedResult<Todo> pagedResult = TodoTestFactory.createPagedResultOfTodos(page, size, totalElements);
        var todoProto = dev.rgonzalez.proto.todo.common.messages.Todo.newBuilder().build();
        when(todoProtoConverter.toProto(any(Todo.class)))
                .thenReturn(Optional.of(todoProto));

        // When
        var result = pagedResultProtoConverter.toProto(pagedResult);

        // Then
        assertThat(result).isPresent();
        ListTodoResponse listTodoResponse = result.get();
        PageResponse pageResponse = listTodoResponse.getPageResponse();
        assertThat(pageResponse.getPage()).isEqualTo(pagedResult.getPage());
        assertThat(pageResponse.getSize()).isEqualTo(pagedResult.getSize());
        assertThat(pageResponse.getTotalPages()).isEqualTo(pagedResult.getTotalPages());
        assertThat(pageResponse.getTotalElements()).isEqualTo(pagedResult.getTotalElements());
        assertThat(listTodoResponse.getTodosList()).hasSameSizeAs(pagedResult.getContent());
    }

}