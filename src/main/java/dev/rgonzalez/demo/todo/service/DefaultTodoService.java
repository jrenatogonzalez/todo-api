package dev.rgonzalez.demo.todo.service;

import dev.rgonzalez.demo.todo.model.PageRequest;
import dev.rgonzalez.demo.todo.model.PagedResult;
import dev.rgonzalez.demo.todo.model.Todo;
import dev.rgonzalez.demo.todo.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DefaultTodoService implements TodoService {
    private final TodoRepository todoRepository;

    @Override
    public PagedResult<Todo> findAll(PageRequest pageRequest) {
        return todoRepository.findAll(pageRequest);
    }

    @Override
    public Optional<Todo> findById(Integer id) {
        return todoRepository.findById(id);
    }

    @Override
    @Transactional
    public Todo create(Todo todo) {
        return todoRepository.create(todo);
    }

    @Override
    @Transactional
    public Todo update(Todo todo) {
        return todoRepository.update(todo);
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        todoRepository.deleteById(id);
    }

}
