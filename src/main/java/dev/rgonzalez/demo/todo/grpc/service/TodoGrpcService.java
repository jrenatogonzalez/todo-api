package dev.rgonzalez.demo.todo.grpc.service;

import dev.rgonzalez.demo.todo.grpc.converter.PageRequestProtoConverter;
import dev.rgonzalez.demo.todo.grpc.converter.PagedResultProtoConverter;
import dev.rgonzalez.demo.todo.grpc.converter.TodoProtoConverter;
import dev.rgonzalez.demo.todo.model.PagedResult;
import dev.rgonzalez.demo.todo.model.Todo;
import dev.rgonzalez.demo.todo.service.TodoService;
import dev.rgonzalez.proto.todo.client.todo.GetTodoRequest;
import dev.rgonzalez.proto.todo.client.todo.GetTodoResponse;
import dev.rgonzalez.proto.todo.client.todo.ListTodoRequest;
import dev.rgonzalez.proto.todo.client.todo.ListTodoResponse;
import dev.rgonzalez.proto.todo.client.todo.TodoServiceGrpc;
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

    @Override
    public void list(ListTodoRequest request, StreamObserver<ListTodoResponse> responseObserver) {
        var optionalPageRequest = pageRequestProtoConverter.toModel(request.getPageRequest());
        if (optionalPageRequest.isPresent()) {
            var pageRequest = optionalPageRequest.get();
            PagedResult<Todo> modelPagedResult = todoService.findAll(pageRequest);
            buildListTodoResponse(responseObserver, modelPagedResult);
        } else {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Invalid PageRequest")
                    .asException());
        }
    }

    private void buildListTodoResponse(StreamObserver<ListTodoResponse> responseObserver, PagedResult<Todo> modelPagedResult) {
        var listTodoResponse = pagedResultProtoConverter.toProto(modelPagedResult);
        if (listTodoResponse.isPresent()) {
            responseObserver.onNext(listTodoResponse.get());
            responseObserver.onCompleted();

        } else {
            responseObserver.onError(Status.ABORTED
                    .withDescription("Couldn't convert to ListTodoResponse type")
                    .asException());
        }
    }

    @Override
    public void get(GetTodoRequest request, StreamObserver<GetTodoResponse> responseObserver) {
        Optional<Todo> optionalTodo = todoService.findById(request.getId());
        if (optionalTodo.isPresent()) {
            buildGetResponse(responseObserver, optionalTodo.get());
        } else {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Todo not found")
                    .asException());
        }
    }

    private void buildGetResponse(StreamObserver<GetTodoResponse> responseObserver, Todo modelTodo) {
        var protoTodo = todoProtoConverter.toProto(modelTodo);
        if (protoTodo.isPresent()) {
            GetTodoResponse response = GetTodoResponse.newBuilder().setTodo(protoTodo.get()).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(Status.ABORTED
                    .withDescription("Couldn't convert to Todo type")
                    .asException());
        }
    }

}
