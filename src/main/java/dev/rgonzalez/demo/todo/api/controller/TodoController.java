package dev.rgonzalez.demo.todo.api.controller;

import dev.rgonzalez.demo.todo.api.converter.TodoDTOConverter;
import dev.rgonzalez.demo.todo.api.domain.CreateTodoRequest;
import dev.rgonzalez.demo.todo.api.domain.TodoDTO;
import dev.rgonzalez.demo.todo.api.domain.UpdateTodoRequest;
import dev.rgonzalez.demo.todo.model.PageRequest;
import dev.rgonzalez.demo.todo.model.PagedResult;
import dev.rgonzalez.demo.todo.model.Todo;
import dev.rgonzalez.demo.todo.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/todos")
@RequiredArgsConstructor
public class TodoController {
    private final TodoService todoService;
    private final TodoDTOConverter converter;

    @GetMapping
    public PagedResult<TodoDTO> findAll(@RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
                                        @RequestParam(name = "size", required = false, defaultValue = "20") Integer size) {
        PagedResult<Todo> pagedResult = todoService.findAll(PageRequest.of(page, size));
        return converter.toDTO(pagedResult);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TodoDTO> findById(@PathVariable("id") Integer id) {
        return todoService.findById(id)
                .map(converter::toDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    ResponseEntity<TodoDTO> create(@RequestBody @Validated CreateTodoRequest request) {
        Todo todo = todoService.create(converter.toModel(request));
        TodoDTO todoDTO = converter.toDTO(todo);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(todoDTO.id())
                .toUri();
        return ResponseEntity.created(location).body(todoDTO);
    }

    @PatchMapping("/{id}")
    ResponseEntity<TodoDTO> update(@PathVariable("id") Integer id,
                                   @RequestBody @Validated UpdateTodoRequest request) {
        Todo todo = todoService.update(converter.toModel(id, request));
        TodoDTO todoDTO = converter.toDTO(todo);
        return ResponseEntity.ok(todoDTO);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable(name = "id") Integer id) {
        todoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
