package dev.rgonzalez.demo.todo.grpc.service.observer;

import dev.rgonzalez.proto.todo.client.todo.ListTodoResponse;
import io.grpc.stub.StreamObserver;

import java.util.Objects;

public class ListTodoResponseObserver implements StreamObserver<ListTodoResponse> {
    private boolean completed = false;
    private Throwable error;
    private ListTodoResponse listTodoResponse;

    @Override
    public void onNext(ListTodoResponse listTodoResponse) {
        this.listTodoResponse = listTodoResponse;
    }

    @Override
    public void onError(Throwable throwable) {
        this.error = throwable;
    }

    @Override
    public void onCompleted() {
        completed = true;
    }

    public ListTodoResponse getListTodoResponse() {
        return listTodoResponse;
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean hasError() {
        return Objects.nonNull(error);
    }

    public Throwable getError() {
        return error;
    }
}
