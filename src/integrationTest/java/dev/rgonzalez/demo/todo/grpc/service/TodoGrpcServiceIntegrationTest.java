package dev.rgonzalez.demo.todo.grpc.service;

import dev.rgonzalez.demo.todo.AppProperties;
import dev.rgonzalez.demo.todo.grpc.service.observer.ListTodoResponseObserver;
import dev.rgonzalez.proto.todo.client.todo.CreateTodoRequest;
import dev.rgonzalez.proto.todo.client.todo.DeleteTodoRequest;
import dev.rgonzalez.proto.todo.client.todo.GetTodoRequest;
import dev.rgonzalez.proto.todo.client.todo.ListTodoRequest;
import dev.rgonzalez.proto.todo.client.todo.ListTodoResponse;
import dev.rgonzalez.proto.todo.client.todo.TodoServiceGrpc;
import dev.rgonzalez.proto.todo.client.todo.UpdateTodoRequest;
import dev.rgonzalez.proto.todo.common.messages.Date;
import dev.rgonzalez.proto.todo.common.messages.PageRequest;
import dev.rgonzalez.proto.todo.common.messages.PageResponse;
import dev.rgonzalez.proto.todo.common.messages.Todo;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static dev.rgonzalez.proto.todo.common.messages.Priority.PRIORITY_HIGH;
import static dev.rgonzalez.proto.todo.common.messages.Priority.PRIORITY_LOW;
import static dev.rgonzalez.proto.todo.common.messages.Priority.PRIORITY_MEDIUM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Sql("/scripts/todo-grpc-test-data.sql")
class TodoGrpcServiceIntegrationTest {
    private static final Logger logger = Logger.getLogger(TodoGrpcServiceIntegrationTest.class.getName());
    private ManagedChannel channel;
    private TodoServiceGrpc.TodoServiceBlockingStub blockingStub;
    private TodoServiceGrpc.TodoServiceStub asyncStub;
    @Autowired
    private AppProperties appProperties;

    @BeforeEach
    public void setUp() {
        channel = ManagedChannelBuilder.forAddress("localhost", appProperties.getGrpcPort()).usePlaintext().build();
        blockingStub = TodoServiceGrpc.newBlockingStub(channel);
        asyncStub = TodoServiceGrpc.newStub(channel);
    }

    @AfterEach
    public void tearDown() {
        try {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, e, () -> "Error shutting down channel");
        }
    }

    @Test
    void shouldGetAListOfTodos() {
        // Given
        int page = 1;
        int size = 5;
        PageRequest pageRequest = PageRequest.newBuilder().setPage(page).setSize(size).build();
        ListTodoRequest request = ListTodoRequest.newBuilder().setPageRequest(pageRequest).build();

        // When
        ListTodoResponse response = blockingStub.list(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTodosList()).isNotNull()
                .hasSize(2);
        PageResponse pageResponse = response.getPageResponse();
        assertThat(pageResponse.getPage()).isEqualTo(page);
        assertThat(pageResponse.getSize()).isEqualTo(size);
        assertThat(pageResponse.getTotalElements()).isEqualTo(7);
        assertThat(pageResponse.getTotalPages()).isEqualTo(2);
    }

    @Test
    void shouldGetAListOfTodosUsingAsynchronousCall() {
        // Given
        int page = 1;
        int size = 5;
        PageRequest pageRequest = PageRequest.newBuilder().setPage(page).setSize(size).build();
        ListTodoRequest request = ListTodoRequest.newBuilder().setPageRequest(pageRequest).build();

        // When
        ListTodoResponseObserver responseObserver = new ListTodoResponseObserver();
        asyncStub.list(request, responseObserver);
        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .until(responseObserver::isCompleted);

        // Then
        assertThat(responseObserver.hasError()).isFalse();
        ListTodoResponse response = responseObserver.getListTodoResponse();
        assertThat(response.getTodosList()).isNotNull()
                .hasSize(2);
        PageResponse pageResponse = response.getPageResponse();
        assertThat(pageResponse.getPage()).isEqualTo(page);
        assertThat(pageResponse.getSize()).isEqualTo(size);
        assertThat(pageResponse.getTotalElements()).isEqualTo(7);
        assertThat(pageResponse.getTotalPages()).isEqualTo(2);
    }

    @Test
    void shouldGetTodoById() {
        // Given
        int id = 5;
        var request = GetTodoRequest.newBuilder().setId(id).build();

        // When
        var response = blockingStub.get(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTodo()).isNotNull();
        assertThat(response.getTodo().getId()).isEqualTo(id);
        assertThat(response.getTodo().getDescription()).isEqualTo("Task DDD");
        assertThat(response.getTodo().getPriority()).isEqualTo(PRIORITY_HIGH);
        var dueDate = response.getTodo().getDueDate();
        assertThat(dueDate).isNotNull();
        assertThat(dueDate.getYear()).isEqualTo(2024);
        assertThat(dueDate.getMonth()).isEqualTo(8);
        assertThat(dueDate.getDay()).isEqualTo(10);
        assertThat(response.getTodo().getCompleted()).isFalse();
        assertThat(response.getTodo().getCreatedAt()).isNotNull();
    }

    @Test
    void shouldThrowNotFoundExceptionWhenSearchingForNonExistentTodo() {
        // Given
        int id = 4;
        var request = GetTodoRequest.newBuilder()
                .setId(id)
                .build();

        // When & Then
        assertThatThrownBy(() -> blockingStub.get(request))
                .isInstanceOf(StatusRuntimeException.class)
                .hasMessage("NOT_FOUND: Todo not found")
                .extracting(e -> (StatusRuntimeException) e)
                .extracting(sre -> sre.getStatus().getCode()).isEqualTo(Status.NOT_FOUND.getCode());
    }

    @Test
    void shouldCreateTodo() {
        // Given
        String description = "Todo to be created";
        Date dueDate = Date.newBuilder()
                .setYear(2024)
                .setMonth(5)
                .setDay(18)
                .build();
        CreateTodoRequest request = CreateTodoRequest.newBuilder()
                .setDescription(description)
                .setPriority(PRIORITY_LOW)
                .setDueDate(dueDate)
                .build();

        // When
        var response = blockingStub.create(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTodo()).isNotNull();
        Todo createdTodo = response.getTodo();
        assertThat(createdTodo.getId()).isPositive();
        assertThat(createdTodo.getDescription()).isEqualTo(description);
        assertThat(createdTodo.getPriority()).isEqualTo(PRIORITY_LOW);
        assertThat(createdTodo.hasDueDate()).isTrue();
        var actualDueDate = response.getTodo().getDueDate();
        assertThat(actualDueDate.getYear()).isEqualTo(2024);
        assertThat(actualDueDate.getMonth()).isEqualTo(5);
        assertThat(actualDueDate.getDay()).isEqualTo(18);
        assertThat(createdTodo.hasCreatedAt()).isTrue();
        assertThat(createdTodo.getCompleted()).isFalse();
    }

    @Test
    void shouldThrowInvalidArgumentExceptionWhenCreatingTodoWithoutDescription() {
        // Given
        String description = "";
        Date dueDate = Date.newBuilder()
                .setYear(2024)
                .setMonth(5)
                .setDay(18)
                .build();
        CreateTodoRequest request = CreateTodoRequest.newBuilder()
                .setDescription(description)
                .setPriority(PRIORITY_LOW)
                .setDueDate(dueDate)
                .build();

        // When
        assertThatThrownBy(() -> blockingStub.create(request))
                .hasMessage("INVALID_ARGUMENT: Description is required.")
                .isInstanceOf(StatusRuntimeException.class)
                .extracting(e -> (StatusRuntimeException) e)
                .extracting(sre -> sre.getStatus().getCode()).isEqualTo(Status.INVALID_ARGUMENT.getCode());;
    }

    @Test
    void shouldUpdateTodo() {
        // Given
        int id = 3;
        String description = "Todo to be updated";
        UpdateTodoRequest request = UpdateTodoRequest.newBuilder()
                .setId(id)
                .setDescription(description)
                .setCompleted(true)
                .build();

        // When
        var response = blockingStub.update(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTodo()).isNotNull();
        Todo updatedTodo = response.getTodo();
        assertThat(updatedTodo.getId()).isEqualTo(id);
        assertThat(updatedTodo.getDescription()).isEqualTo(description);
        assertThat(updatedTodo.getPriority()).isEqualTo(PRIORITY_MEDIUM);
        assertThat(updatedTodo.hasDueDate()).isFalse();
        assertThat(updatedTodo.hasCreatedAt()).isTrue();
        assertThat(updatedTodo.getCompleted()).isTrue();
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUpdatingNonExistentTodo() {
        // Given
        int id = 4;
        String description = "Todo to be updated";
        UpdateTodoRequest request = UpdateTodoRequest.newBuilder()
                .setId(id)
                .setDescription(description)
                .setCompleted(true)
                .build();

        // When & Then
        assertThatThrownBy(() -> blockingStub.update(request))
                .isInstanceOf(StatusRuntimeException.class)
                .hasMessage("NOT_FOUND: Todo not found")
                .extracting(e -> (StatusRuntimeException) e)
                .extracting(sre -> sre.getStatus().getCode()).isEqualTo(Status.NOT_FOUND.getCode());
    }

    @Test
    void shouldDeleteTodo() {
        // Given
        int id = 7;
        var request = DeleteTodoRequest.newBuilder().setId(id).build();

        // When
        var response = blockingStub.delete(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.isInitialized()).isTrue();
    }

    @Test
    void shouldThrowNotFoundExceptionWhenDeletingNonExistentTodo() {
        // Given
        int id = 4;
        var request = DeleteTodoRequest.newBuilder().setId(id).build();

        // When & then
        assertThatThrownBy(() -> blockingStub.delete(request))
                .isInstanceOf(StatusRuntimeException.class)
                .hasMessage("NOT_FOUND: Todo not found")
                .extracting(e -> (StatusRuntimeException) e)
                .extracting(sre -> sre.getStatus().getCode()).isEqualTo(Status.NOT_FOUND.getCode());
    }

}
