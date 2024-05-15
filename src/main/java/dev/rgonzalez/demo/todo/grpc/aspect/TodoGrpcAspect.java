package dev.rgonzalez.demo.todo.grpc.aspect;

import dev.rgonzalez.proto.todo.client.todo.CreateTodoRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Component
@Aspect
@RequiredArgsConstructor
public class TodoGrpcAspect {
    private final ValidatorService validatorService;

    @Before(value = "execution(public * dev.rgonzalez.demo.todo.grpc.service.TodoGrpcService.create(..)) && args(request,..)")
    public void beforeCreateTodoRequest(CreateTodoRequest request) {
        var createTodoRequestWrapper = CreateTodoRequestWrapper.builder()
                .description(request.getDescription())
                .build();

        validatorService.validate(createTodoRequestWrapper);
    }

}
