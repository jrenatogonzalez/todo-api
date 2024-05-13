package dev.rgonzalez.demo.todo.service;

import dev.rgonzalez.demo.todo.model.PageRequest;
import dev.rgonzalez.demo.todo.model.PagedResult;
import dev.rgonzalez.demo.todo.model.Todo;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface TodoService {
    PagedResult<Todo> findAll(PageRequest pageRequest);

    Optional<Todo> findById(Integer id);

    @Transactional
    Todo create(Todo todo);

    @Transactional
    Todo update(Todo todo);

    @Transactional
    void deleteById(Integer id);
}
