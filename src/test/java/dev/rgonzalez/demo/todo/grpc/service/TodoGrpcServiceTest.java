package dev.rgonzalez.demo.todo.grpc.service;

import dev.rgonzalez.demo.todo.grpc.converter.CreateTodoRequestProtoConverter;
import dev.rgonzalez.demo.todo.grpc.converter.PageRequestProtoConverter;
import dev.rgonzalez.demo.todo.grpc.converter.PagedResultProtoConverter;
import dev.rgonzalez.demo.todo.grpc.converter.TodoProtoConverter;
import dev.rgonzalez.demo.todo.grpc.converter.UpdateTodoRequestProtoConverter;
import dev.rgonzalez.demo.todo.model.PagedResult;
import dev.rgonzalez.demo.todo.model.Todo;
import dev.rgonzalez.demo.todo.service.TodoService;
import dev.rgonzalez.demo.todo.test.util.TodoTestFactory;
import dev.rgonzalez.proto.todo.client.todo.CreateTodoRequest;
import dev.rgonzalez.proto.todo.client.todo.CreateTodoResponse;
import dev.rgonzalez.proto.todo.client.todo.DeleteTodoRequest;
import dev.rgonzalez.proto.todo.client.todo.DeleteTodoResponse;
import dev.rgonzalez.proto.todo.client.todo.GetTodoRequest;
import dev.rgonzalez.proto.todo.client.todo.GetTodoResponse;
import dev.rgonzalez.proto.todo.client.todo.ListTodoRequest;
import dev.rgonzalez.proto.todo.client.todo.ListTodoResponse;
import dev.rgonzalez.proto.todo.client.todo.UpdateTodoRequest;
import dev.rgonzalez.proto.todo.client.todo.UpdateTodoResponse;
import dev.rgonzalez.proto.todo.common.messages.PageRequest;
import dev.rgonzalez.proto.todo.common.messages.Priority;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TodoGrpcServiceTest {
    @Mock
    private TodoService todoService;

    @Mock
    private PagedResultProtoConverter pagedResultProtoConverter;

    @Mock
    private PageRequestProtoConverter pageRequestProtoConverter;

    @Mock
    private TodoProtoConverter todoProtoConverter;

    @Mock
    private CreateTodoRequestProtoConverter createTodoRequestProtoConverter;

    @Mock
    private UpdateTodoRequestProtoConverter updateTodoRequestProtoConverter;

    @InjectMocks
    private TodoGrpcService todoGrpcService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldListTodos() {
        // Given
        int page = 0;
        int size = 10;
        var pageRequest = PageRequest.newBuilder().setPage(page).setSize(size).build();
        var request = ListTodoRequest.newBuilder().setPageRequest(pageRequest).build();
        StreamObserver<ListTodoResponse> responseObserver = Mockito.mock(StreamObserver.class);
        var modelPageRequest = dev.rgonzalez.demo.todo.model.PageRequest.of(page, size);
        when(pageRequestProtoConverter.toModel(pageRequest))
                .thenReturn(Optional.of(modelPageRequest));
        var pagedResult = new PagedResult<>(TodoTestFactory.createTodoList(),
                120, modelPageRequest);
        when(todoService.findAll(modelPageRequest))
                .thenReturn(pagedResult);
        var listTodoResponse = ListTodoResponse.newBuilder().build();
        when(pagedResultProtoConverter.toProto(pagedResult))
                .thenReturn(Optional.of(listTodoResponse));

        // When
        todoGrpcService.list(request, responseObserver);

        // Then
        verify(todoService).findAll(modelPageRequest);
        verify(responseObserver).onNext(any(ListTodoResponse.class));
    }

    @Test
    void shouldGetATodo() {
        // Given
        int id = 7;
        var request = GetTodoRequest.newBuilder().setId(id).build();
        StreamObserver<GetTodoResponse> responseObserver = Mockito.mock(StreamObserver.class);
        Todo modelTodo = Todo.builder()
                .id(id)
                .description("An existing Todo")
                .priority(dev.rgonzalez.demo.todo.model.Priority.HIGH)
                .build();
        when(todoService.findById(id)).thenReturn(Optional.of(modelTodo));
        var protoTodo = dev.rgonzalez.proto.todo.common.messages.Todo.newBuilder().build();
        when(todoProtoConverter.toProto(modelTodo)).thenReturn(Optional.of(protoTodo));

        // When
        todoGrpcService.get(request, responseObserver);

        // Then
        verify(todoService).findById(id);
        verify(responseObserver).onNext(any(GetTodoResponse.class));
    }

    @Test
    void shouldCreateTodo() {
        // Given
        String description = "A new task";
        var request = CreateTodoRequest.newBuilder()
                .setDescription(description)
                .setPriority(Priority.PRIORITY_HIGH)
                .build();
        StreamObserver<CreateTodoResponse> responseObserver = Mockito.mock(StreamObserver.class);
        Todo modelTodo = Todo.builder()
                .description(description)
                .priority(dev.rgonzalez.demo.todo.model.Priority.HIGH)
                .build();
        when(createTodoRequestProtoConverter.toModel(request)).thenReturn(Optional.of(modelTodo));
        var protoTodo = dev.rgonzalez.proto.todo.common.messages.Todo.newBuilder().build();
        when(todoService.create(modelTodo)).thenReturn(modelTodo);
        when(todoProtoConverter.toProto(modelTodo))
                .thenReturn(Optional.of(protoTodo));

        // When
        todoGrpcService.create(request, responseObserver);

        // Then
        verify(todoService).create(modelTodo);
        verify(responseObserver).onNext(any(CreateTodoResponse.class));
    }


    @Test
    void shouldUpdateTodo() {
        // Given
        int id = 5;
        String description = "A new description for task";
        var request = UpdateTodoRequest.newBuilder().setId(id)
                .setDescription(description)
                .setCompleted(true)
                .build();
        StreamObserver<UpdateTodoResponse> responseObserver = Mockito.mock(StreamObserver.class);
        Todo modelTodo = Todo.builder()
                .id(id)
                .description(description)
                .completed(true)
                .build();
        when(updateTodoRequestProtoConverter.toModel(request)).thenReturn(Optional.of(modelTodo));
        var protoTodo = dev.rgonzalez.proto.todo.common.messages.Todo.newBuilder().build();
        when(todoService.update(modelTodo)).thenReturn(modelTodo);
        when(todoProtoConverter.toProto(modelTodo))
                .thenReturn(Optional.of(protoTodo));

        // When
        todoGrpcService.update(request, responseObserver);

        // Then
        verify(todoService).update(modelTodo);
        verify(responseObserver).onNext(any(UpdateTodoResponse.class));
    }

    @Test
    void shouldDeleteTodo() {
        // Given
        int id = 5;
        var request = DeleteTodoRequest.newBuilder().setId(id).build();
        StreamObserver<DeleteTodoResponse> responseObserver = Mockito.mock(StreamObserver.class);

        // When
        todoGrpcService.delete(request, responseObserver);

        // Then
        verify(todoService).deleteById(id);
        verify(responseObserver).onNext(any(DeleteTodoResponse.class));
    }

}