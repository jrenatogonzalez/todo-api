package dev.rgonzalez.demo.todo.grpc.aspect;

import dev.rgonzalez.proto.todo.client.todo.CreateTodoRequest;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class TodoGrpcAspect {

    @Before(value = "execution(public * dev.rgonzalez.demo.todo.grpc.service.TodoGrpcService.create(..)) && args(request,..)")
    public void beforeCreateTodoRequest(CreateTodoRequest request) {
        var createTodoRequestWrapper = CreateTodoRequestWrapper.builder()
                .description(request.getDescription())
                .build();

        ValidatorService.getInstance().validate(createTodoRequestWrapper);
    }

}
