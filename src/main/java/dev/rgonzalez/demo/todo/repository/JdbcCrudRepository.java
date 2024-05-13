package dev.rgonzalez.demo.todo.repository;

import dev.rgonzalez.demo.todo.model.PageRequest;
import dev.rgonzalez.demo.todo.model.PagedResult;

import java.util.Optional;

public interface JdbcCrudRepository<T, K> {
    PagedResult<T> findAll(PageRequest pageRequest);

    Optional<T> findById(K id);

    T create(T todo);

    T update(T todo);

    void deleteById(K id);

    int count();
}
