package dev.rgonzalez.demo.todo;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;

@Getter
public class AppProperties {

    @Value("${grpc.port:50051}")
    private int grpcPort;

    @Value("${termination.grace.period.seconds:30}")
    private long terminationGracePeriodSeconds;

}
