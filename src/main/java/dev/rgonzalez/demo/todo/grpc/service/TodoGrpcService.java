package dev.rgonzalez.demo.todo.grpc.service;

import dev.rgonzalez.demo.todo.exceptions.NotFoundException;
import dev.rgonzalez.demo.todo.grpc.converter.CreateTodoRequestProtoConverter;
import dev.rgonzalez.demo.todo.grpc.converter.PageRequestProtoConverter;
import dev.rgonzalez.demo.todo.grpc.converter.PagedResultProtoConverter;
import dev.rgonzalez.demo.todo.grpc.converter.TodoProtoConverter;
import dev.rgonzalez.demo.todo.grpc.converter.UpdateTodoRequestProtoConverter;
import dev.rgonzalez.demo.todo.model.PagedResult;
import dev.rgonzalez.demo.todo.model.Todo;
import dev.rgonzalez.demo.todo.service.TodoService;
import dev.rgonzalez.proto.todo.client.todo.CreateTodoRequest;
import dev.rgonzalez.proto.todo.client.todo.CreateTodoResponse;
import dev.rgonzalez.proto.todo.client.todo.DeleteTodoRequest;
import dev.rgonzalez.proto.todo.client.todo.DeleteTodoResponse;
import dev.rgonzalez.proto.todo.client.todo.GetTodoRequest;
import dev.rgonzalez.proto.todo.client.todo.GetTodoResponse;
import dev.rgonzalez.proto.todo.client.todo.ListTodoRequest;
import dev.rgonzalez.proto.todo.client.todo.ListTodoResponse;
import dev.rgonzalez.proto.todo.client.todo.TodoServiceGrpc;
import dev.rgonzalez.proto.todo.client.todo.UpdateTodoRequest;
import dev.rgonzalez.proto.todo.client.todo.UpdateTodoResponse;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TodoGrpcService extends TodoServiceGrpc.TodoServiceImplBase {
    private final TodoService todoService;
    private final PageRequestProtoConverter pageRequestProtoConverter;
    private final PagedResultProtoConverter pagedResultProtoConverter;
    private final TodoProtoConverter todoProtoConverter;
    private final CreateTodoRequestProtoConverter createTodoRequestProtoConverter;
    private final UpdateTodoRequestProtoConverter updateTodoRequestProtoConverter;

    private static final String ERROR_CONVERTING_TODO_TYPE = "Couldn't convert to Todo type";
    private static final String ERROR_CONVERTING_LIST_TODO_TYPE = "Couldn't convert to ListTodoResponse type";

    @Override
    public void list(ListTodoRequest request, StreamObserver<ListTodoResponse> responseObserver) {
        var optionalPageRequest = pageRequestProtoConverter.toModel(request.getPageRequest());
        if (optionalPageRequest.isPresent()) {
            var pageRequest = optionalPageRequest.get();
            PagedResult<Todo> modelPagedResult = todoService.findAll(pageRequest);
            buildListTodoResponse(responseObserver, modelPagedResult);
        } else {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Invalid PageRequest").asException());
        }
    }

    private void buildListTodoResponse(StreamObserver<ListTodoResponse> responseObserver, PagedResult<Todo> modelPagedResult) {
        var listTodoResponse = pagedResultProtoConverter.toProto(modelPagedResult);
        if (listTodoResponse.isPresent()) {
            responseObserver.onNext(listTodoResponse.get());
            responseObserver.onCompleted();

        } else {
            reportErrorConvertingTypes(responseObserver, ERROR_CONVERTING_LIST_TODO_TYPE);
        }
    }

    @Override
    public void get(GetTodoRequest request, StreamObserver<GetTodoResponse> responseObserver) {
        Optional<Todo> optionalTodo = todoService.findById(request.getId());
        if (optionalTodo.isPresent()) {
            buildGetResponse(responseObserver, optionalTodo.get());
        } else {
            responseObserver.onError(Status.NOT_FOUND.withDescription("Todo not found").asException());
        }
    }

    private void buildGetResponse(StreamObserver<GetTodoResponse> responseObserver, Todo modelTodo) {
        var protoTodo = todoProtoConverter.toProto(modelTodo);
        if (protoTodo.isPresent()) {
            GetTodoResponse response = GetTodoResponse.newBuilder().setTodo(protoTodo.get()).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } else {
            reportErrorConvertingTypes(responseObserver, ERROR_CONVERTING_TODO_TYPE);
        }
    }

    @Override
    public void create(CreateTodoRequest request, StreamObserver<CreateTodoResponse> responseObserver) {
        var modelTodo = createTodoRequestProtoConverter.toModel(request);
        if (modelTodo.isPresent()) {
            Todo createdModelTodo = todoService.create(modelTodo.get());
            var createdProtoTodo = todoProtoConverter.toProto(createdModelTodo);
            buildCreateResponse(responseObserver, createdProtoTodo);
        } else {
            reportErrorConvertingTypes(responseObserver, ERROR_CONVERTING_TODO_TYPE);
        }
    }

    private void buildCreateResponse(StreamObserver<CreateTodoResponse> responseObserver, Optional<dev.rgonzalez.proto.todo.common.messages.Todo> createdProtoTodo) {
        if (createdProtoTodo.isPresent()) {
            CreateTodoResponse response = CreateTodoResponse.newBuilder()
                    .setTodo(createdProtoTodo.get())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } else {
            reportErrorConvertingTypes(responseObserver, ERROR_CONVERTING_TODO_TYPE);
        }
    }

    private void reportErrorConvertingTypes(StreamObserver responseObserver, String message) {
        responseObserver.onError(Status.ABORTED.withDescription(message).asException());
    }

    @Override
    public void update(UpdateTodoRequest request, StreamObserver<UpdateTodoResponse> responseObserver) {
        var modelTodo = updateTodoRequestProtoConverter.toModel(request);
        if (modelTodo.isPresent()) {
            Todo updatedModelTodo = todoService.update(modelTodo.get());
            var updatedProtoTodo = todoProtoConverter.toProto(updatedModelTodo);
            buildUpdateResponse(responseObserver, updatedProtoTodo);
        } else {
            reportErrorConvertingTypes(responseObserver, ERROR_CONVERTING_TODO_TYPE);
        }
    }

    private void buildUpdateResponse(StreamObserver<UpdateTodoResponse> responseObserver, Optional<dev.rgonzalez.proto.todo.common.messages.Todo> updatedProtoTodo) {
        if (updatedProtoTodo.isPresent()) {
            UpdateTodoResponse response = UpdateTodoResponse.newBuilder()
                    .setTodo(updatedProtoTodo.get())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } else {
            reportErrorConvertingTypes(responseObserver, ERROR_CONVERTING_TODO_TYPE);
        }
    }

    @Override
    public void delete(DeleteTodoRequest request, StreamObserver<DeleteTodoResponse> responseObserver) {
        try {
            todoService.deleteById(request.getId());
            var response = DeleteTodoResponse.newBuilder().build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (NotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asException());
        }
    }
}
