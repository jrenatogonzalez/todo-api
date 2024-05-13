package dev.rgonzalez.demo.todo.repository;

import dev.rgonzalez.demo.todo.exceptions.NotFoundException;
import dev.rgonzalez.demo.todo.model.PageRequest;
import dev.rgonzalez.demo.todo.model.PagedResult;
import dev.rgonzalez.demo.todo.model.Todo;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Repository
@RequiredArgsConstructor
public class TodoRepository implements JdbcCrudRepository<Todo, Integer> {
    private final JdbcClient jdbcClient;
    private final Supplier<NotFoundException> notFoundException = () -> new NotFoundException("Todo not found");

    private static final String SQL_FIND_ALL = """
            SELECT id, description, priority, due_date, completed, created_at, completed_at
              FROM todo_item
             LIMIT :limit OFFSET :offset
            """;
    private static final String SQL_CREATE_TODO = """
            INSERT INTO todo_item (description, priority, due_date)
            VALUES (:description, :priority, :dueDate)
            RETURNING id, description, priority, due_date, completed, created_at, completed_at
            """;
    private static final String SQL_FIND_BY_ID = """
            SELECT id, description, priority, due_date, completed, created_at, completed_at
              FROM todo_item
             WHERE id = :id
            """;
    private static final String SQL_DELETE_BY_ID = """
            DELETE FROM todo_item
             WHERE id = :id
            """;
    private static final String SQL_UPDATE_TODO = """
            UPDATE todo_item
               SET description = :description,
                   priority = :priority,
                   due_date = :dueDate,
                   completed = :completed,
                   completed_at = :completed_at
             WHERE id = :id
            RETURNING id, description, priority, due_date, completed, created_at, completed_at
            """;
    private static final String SQL_TODO_COUNT = """
            SELECT COUNT(*) FROM todo_item
            """;

    @Override
    public PagedResult<Todo> findAll(PageRequest pageRequest) {
        int count = count();
        List<Todo> todoList = jdbcClient.sql(SQL_FIND_ALL)
                .param("limit", pageRequest.getSize())
                .param("offset", pageRequest.getOffset())
                .query(Todo.class)
                .list();
        return new PagedResult<>(todoList, count, pageRequest);
    }

    @Override
    public int count() {
        return (int) jdbcClient.sql(SQL_TODO_COUNT)
                .query()
                .singleValue();
    }

    @Override
    public Optional<Todo> findById(Integer id) {
        return jdbcClient.sql(SQL_FIND_BY_ID)
                .param("id", id)
                .query(Todo.class)
                .optional();
    }

    @Override
    public Todo create(Todo todo) {
        return jdbcClient.sql(SQL_CREATE_TODO)
                .param("description", todo.getDescription())
                .param("priority", todo.getPriority())
                .param("dueDate", todo.getDueDate())
                .query(Todo.class)
                .single();
    }

    @Override
    public Todo update(Todo todo) {
        Todo originalTodo = findById(todo.getId())
                .orElseThrow(notFoundException);

        Optional<Todo> optionalTodo = jdbcClient.sql(SQL_UPDATE_TODO)
                .param("id", todo.getId())
                .param("description", Optional.ofNullable(todo.getDescription()).orElse(originalTodo.getDescription()))
                .param("priority", Optional.ofNullable(todo.getPriority()).orElse(originalTodo.getPriority()))
                .param("dueDate", Optional.ofNullable(todo.getDueDate()).orElse(originalTodo.getDueDate()))
                .param("completed", todo.isCompleted())
                .param("completed_at", todo.isCompleted() ? LocalDateTime.now() : null)
                .query(Todo.class)
                .optional();

        return optionalTodo.orElseThrow(notFoundException);
    }

    @Override
    public void deleteById(Integer id) {
        int deletedRows = jdbcClient.sql(SQL_DELETE_BY_ID)
                .param("id", id)
                .update();
        if (deletedRows == 0) {
            throw notFoundException.get();
        }
    }

}
