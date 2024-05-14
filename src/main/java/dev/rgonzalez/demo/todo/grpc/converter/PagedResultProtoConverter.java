package dev.rgonzalez.demo.todo.grpc.converter;

import dev.rgonzalez.demo.todo.model.PagedResult;
import dev.rgonzalez.demo.todo.model.Todo;
import dev.rgonzalez.proto.todo.client.todo.ListTodoResponse;
import dev.rgonzalez.proto.todo.common.messages.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PagedResultProtoConverter implements ProtoConverter<ListTodoResponse, PagedResult<Todo>> {
    private final TodoProtoConverter todoProtoConverter;

    @Override
    public Optional<ListTodoResponse> toProto(PagedResult<Todo> model) {
        if (Objects.isNull(model)) {
            return Optional.empty();
        }
        PageResponse pageResponse = toPageResponse(model);

        List<dev.rgonzalez.proto.todo.common.messages.Todo> protoTodoList = model.getContent()
                .stream()
                .map(todoProtoConverter::toProto)
                .flatMap(Optional::stream)
                .toList();

        return Optional.of(
                ListTodoResponse.newBuilder()
                        .setPageResponse(pageResponse)
                        .addAllTodos(protoTodoList)
                        .build()
        );
    }

    private PageResponse toPageResponse(PagedResult<Todo> pagedResult) {
        return PageResponse.newBuilder()
                .setPage(pagedResult.getPage())
                .setSize(pagedResult.getSize())
                .setTotalElements(pagedResult.getTotalElements())
                .setTotalPages(pagedResult.getTotalPages())
                .build();
    }
}
