package dev.rgonzalez.demo.todo.repository;

import dev.rgonzalez.demo.todo.exceptions.NotFoundException;
import dev.rgonzalez.demo.todo.model.PageRequest;
import dev.rgonzalez.demo.todo.model.PagedResult;
import dev.rgonzalez.demo.todo.model.Priority;
import dev.rgonzalez.demo.todo.model.Todo;
import dev.rgonzalez.demo.todo.test.util.TodoTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TodoRepositoryTest {
    @Mock
    private JdbcClient jdbcClient;

    @InjectMocks
    private TodoRepository todoRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldGetPagedResultWhenFindingAllTodos() {
        // Given
        String sqlCount = "SELECT COUNT(*) FROM todo_item";
        JdbcClient.StatementSpec countStmtSpec = Mockito.mock(JdbcClient.StatementSpec.class);
        JdbcClient.ResultQuerySpec countResultQuerySpec = Mockito.mock(JdbcClient.ResultQuerySpec.class);
        when(jdbcClient.sql(contains(sqlCount))).thenReturn(countStmtSpec);
        when(countStmtSpec.query()).thenReturn(countResultQuerySpec);
        when(countResultQuerySpec.singleValue()).thenReturn(5);

        List<Todo> todoList = TodoTestFactory.createTodoList();
        int page = 0;
        int size = 5;
        int offset = 0;
        String sqlFindAll = "LIMIT :limit OFFSET :offset";
        JdbcClient.StatementSpec findAllStmtSpec = Mockito.mock(JdbcClient.StatementSpec.class);
        JdbcClient.MappedQuerySpec<Todo> mappedQuerySpec = Mockito.mock(JdbcClient.MappedQuerySpec.class);
        when(jdbcClient.sql(contains(sqlFindAll))).thenReturn(findAllStmtSpec);
        when(findAllStmtSpec.param("limit", size)).thenReturn(findAllStmtSpec);
        when(findAllStmtSpec.param("offset", offset)).thenReturn(findAllStmtSpec);
        when(findAllStmtSpec.query(Todo.class)).thenReturn(mappedQuerySpec);
        when(mappedQuerySpec.list()).thenReturn(todoList);
        PageRequest pageRequest = PageRequest.of(page, size);

        // When
        PagedResult<Todo> pagedResult = todoRepository.findAll(pageRequest);

        // Then
        assertThat(pagedResult).isNotNull();
        assertThat(pagedResult.getPage()).isEqualTo(page);
        assertThat(pagedResult.getSize()).isEqualTo(size);
        assertThat(pagedResult.getTotalPages()).isEqualTo(1);
        assertThat(pagedResult.getTotalElements()).isEqualTo(5);
        assertThat(pagedResult.getContent()).hasSameSizeAs(todoList);
        verify(findAllStmtSpec).param("limit", size);
        verify(findAllStmtSpec).param("offset", offset);
    }

    @Test
    void shouldReturnTheNumberOfRowsInTodoTable() {
        // Given
        String sqlCount = "SELECT COUNT(*) FROM todo_item";
        JdbcClient.StatementSpec statementSpec = Mockito.mock(JdbcClient.StatementSpec.class);
        JdbcClient.ResultQuerySpec resultQuerySpec = Mockito.mock(JdbcClient.ResultQuerySpec.class);
        when(jdbcClient.sql(contains(sqlCount))).thenReturn(statementSpec);
        when(statementSpec.query()).thenReturn(resultQuerySpec);
        when(resultQuerySpec.singleValue()).thenReturn(5);

        // When
        int result = todoRepository.count();

        // Then
        assertThat(result).isEqualTo(5);
        verify(jdbcClient).sql(any(String.class));
    }

    @Test
    void shouldFetchOptionalTodoWhenFindingById() {
        // Given
        Integer id = 5;
        LocalDate dueDate = LocalDate.of(2024, 5, 17);
        Todo todo = new Todo(id, "A todo", Priority.MEDIUM, dueDate,
                false, LocalDateTime.now(), null);

        JdbcClient.StatementSpec statementSpec = Mockito.mock(JdbcClient.StatementSpec.class);
        JdbcClient.MappedQuerySpec<Todo> mappedQuerySpec = Mockito.mock(JdbcClient.MappedQuerySpec.class);
        when(jdbcClient.sql(any(String.class))).thenReturn(statementSpec);
        when(statementSpec.param("id", id)).thenReturn(statementSpec);
        when(statementSpec.query(Todo.class)).thenReturn(mappedQuerySpec);
        when(mappedQuerySpec.optional()).thenReturn(Optional.of(todo));

        // When
        Optional<Todo> result = todoRepository.findById(id);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(id);
        assertThat(result.get().getDescription()).isEqualTo("A todo");
        verify(jdbcClient).sql(any(String.class));
    }

    @Test
    void shouldGetEmptyOptionalWhenFindingNonExistingTodo() {
        // Given
        Integer id = 6;
        String sqlFindById = "SELECT id, description, priority, due_date, completed, created_at, completed_at";
        JdbcClient.StatementSpec statementSpec = Mockito.mock(JdbcClient.StatementSpec.class);
        JdbcClient.MappedQuerySpec<Todo> mappedQuerySpec = Mockito.mock(JdbcClient.MappedQuerySpec.class);
        when(jdbcClient.sql(contains(sqlFindById))).thenReturn(statementSpec);
        when(statementSpec.param("id", id)).thenReturn(statementSpec);
        when(statementSpec.query(Todo.class)).thenReturn(mappedQuerySpec);
        when(mappedQuerySpec.optional()).thenReturn(Optional.empty());

        // When
        Optional<Todo> result = todoRepository.findById(id);

        // Then
        assertThat(result).isEmpty();
        verify(jdbcClient).sql(any(String.class));
    }

    @Test
    void shouldCreateANewTodo() {
        // Given
        Integer id = 17;
        LocalDate dueDate = LocalDate.of(2024, 5, 17);
        String description = "A new Todo";
        Todo todoToBeCreated = Todo.builder()
                .description(description)
                .priority(Priority.LOW)
                .dueDate(dueDate)
                .build();
        Todo createdTodo = new Todo(id, description, Priority.LOW, dueDate, false,
                LocalDateTime.now(), null);
        JdbcClient.StatementSpec statementSpec = Mockito.mock(JdbcClient.StatementSpec.class);
        JdbcClient.MappedQuerySpec<Todo> mappedQuerySpec = Mockito.mock(JdbcClient.MappedQuerySpec.class);
        when(jdbcClient.sql(any(String.class))).thenReturn(statementSpec);
        when(statementSpec.param("description", description)).thenReturn(statementSpec);
        when(statementSpec.param("priority", Priority.LOW)).thenReturn(statementSpec);
        when(statementSpec.param("dueDate", dueDate)).thenReturn(statementSpec);
        when(statementSpec.query(Todo.class)).thenReturn(mappedQuerySpec);
        when(mappedQuerySpec.single()).thenReturn(createdTodo);

        // When
        Todo result = todoRepository.create(todoToBeCreated);

        // Then
        verify(jdbcClient).sql(any(String.class));
        assertThat(result).isEqualTo(createdTodo);
    }

    @Test
    void shouldUpdateExistingTodo() {
        // Given
        Integer id = 6;
        LocalDate dueDate = LocalDate.of(2024, 5, 17);
        Todo originalTodo = new Todo(id, "Original Description", Priority.LOW,
                dueDate, false, LocalDateTime.now(), null);
        Todo todoToBeUpdated = Todo.builder()
                .id(id)
                .description("A new description for Todo")
                .build();
        Todo updatedTodo = new Todo(id, "A new description for Todo", Priority.LOW,
                dueDate, false, LocalDateTime.now(), null);

        String sqlFindById = "SELECT id, description, priority, due_date, completed, created_at, completed_at";
        JdbcClient.StatementSpec findByIdStmtSpec = Mockito.mock(JdbcClient.StatementSpec.class);
        JdbcClient.MappedQuerySpec<Todo> findByIdMappedQuerySpec = Mockito.mock(JdbcClient.MappedQuerySpec.class);
        when(jdbcClient.sql(contains(sqlFindById))).thenReturn(findByIdStmtSpec);
        when(findByIdStmtSpec.param("id", id)).thenReturn(findByIdStmtSpec);
        when(findByIdStmtSpec.query(Todo.class)).thenReturn(findByIdMappedQuerySpec);
        when(findByIdMappedQuerySpec.optional()).thenReturn(Optional.of(originalTodo));

        String sqlUpdateTodo = "UPDATE todo_item";
        JdbcClient.StatementSpec updateStmtSpec = Mockito.mock(JdbcClient.StatementSpec.class);
        JdbcClient.MappedQuerySpec<Todo> updateMappedQuerySpec = Mockito.mock(JdbcClient.MappedQuerySpec.class);
        when(jdbcClient.sql(contains(sqlUpdateTodo))).thenReturn(updateStmtSpec);
        when(updateStmtSpec.param("id", id)).thenReturn(updateStmtSpec);
        when(updateStmtSpec.param(any(String.class), any())).thenReturn(updateStmtSpec);
        when(updateStmtSpec.query(Todo.class)).thenReturn(updateMappedQuerySpec);
        when(updateMappedQuerySpec.optional()).thenReturn(Optional.of(updatedTodo));

        // When & Then
        Todo result = todoRepository.update(todoToBeUpdated);

        // Then
        assertThat(result).isNotNull().isEqualTo(updatedTodo);
        verify(jdbcClient).sql(contains(sqlFindById));
        verify(jdbcClient).sql(contains(sqlUpdateTodo));
    }


    @Test
    void shouldThrowNotFoundExceptionWhenUpdatingNonExistingTodo() {
        // Given
        Integer id = 6;
        Todo todo = Todo.builder()
                .id(id)
                .description("A new description for Todo")
                .build();
        String sqlFindById = "SELECT id, description, priority, due_date, completed, created_at, completed_at";
        JdbcClient.StatementSpec findByIdStmtSpec = Mockito.mock(JdbcClient.StatementSpec.class);
        JdbcClient.MappedQuerySpec<Todo> mappedQuerySpec = Mockito.mock(JdbcClient.MappedQuerySpec.class);
        when(jdbcClient.sql(contains(sqlFindById))).thenReturn(findByIdStmtSpec);
        when(findByIdStmtSpec.param("id", id)).thenReturn(findByIdStmtSpec);
        when(findByIdStmtSpec.query(Todo.class)).thenReturn(mappedQuerySpec);
        when(mappedQuerySpec.optional()).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> todoRepository.update(todo))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Todo not found");

        // Then
        verify(jdbcClient).sql(contains(sqlFindById));
    }

    @Test
    void shouldDeleteTodoById() {
        // Given
        Integer id = 5;
        JdbcClient.StatementSpec statementSpec = Mockito.mock(JdbcClient.StatementSpec.class);
        when(jdbcClient.sql(any(String.class))).thenReturn(statementSpec);
        when(statementSpec.param("id", id)).thenReturn(statementSpec);
        when(statementSpec.update()).thenReturn(1);

        // When
        todoRepository.deleteById(id);

        // Then
        verify(jdbcClient).sql(any(String.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenDeleteNonExistingTodo() {
        // Given
        Integer id = 6;
        JdbcClient.StatementSpec statementSpec = Mockito.mock(JdbcClient.StatementSpec.class);
        when(jdbcClient.sql(any(String.class))).thenReturn(statementSpec);
        when(statementSpec.param("id", id)).thenReturn(statementSpec);
        when(statementSpec.update()).thenReturn(0);

        // When
        assertThatThrownBy(() -> todoRepository.deleteById(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Todo not found");

        // Then
        verify(jdbcClient).sql(any(String.class));
    }

}