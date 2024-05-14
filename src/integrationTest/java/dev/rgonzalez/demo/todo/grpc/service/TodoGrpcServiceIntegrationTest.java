package dev.rgonzalez.demo.todo.grpc.service;

import dev.rgonzalez.demo.todo.AppProperties;
import dev.rgonzalez.demo.todo.grpc.service.observer.ListTodoResponseObserver;
import dev.rgonzalez.proto.todo.client.todo.GetTodoRequest;
import dev.rgonzalez.proto.todo.client.todo.ListTodoRequest;
import dev.rgonzalez.proto.todo.client.todo.ListTodoResponse;
import dev.rgonzalez.proto.todo.client.todo.TodoServiceGrpc;
import dev.rgonzalez.proto.todo.common.messages.PageRequest;
import dev.rgonzalez.proto.todo.common.messages.PageResponse;
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
        var request = GetTodoRequest.newBuilder().setId(id).build();

        // When & Then
        assertThatThrownBy(() -> blockingStub.get(request), "Todo not found")
                .isInstanceOf(StatusRuntimeException.class)
                .extracting(e -> (StatusRuntimeException) e)
                .extracting(sre -> sre.getStatus().getCode()).isEqualTo(Status.NOT_FOUND.getCode());
    }

}
