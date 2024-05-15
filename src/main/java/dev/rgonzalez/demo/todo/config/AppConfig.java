package dev.rgonzalez.demo.todo.config;

import dev.rgonzalez.demo.todo.AppProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@Slf4j
public class AppConfig {

    @Bean
    public AppProperties appProperties() {
        return new AppProperties();
    }

    @Bean(destroyMethod = "shutdown")
    public ExecutorService grpcExecutor() {
        return Executors.newThreadPerTaskExecutor(
                Thread.ofVirtual()
                        .name("grpc-handler")
                        .factory()
        );
    }

}
