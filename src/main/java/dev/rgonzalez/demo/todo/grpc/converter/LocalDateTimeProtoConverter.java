package dev.rgonzalez.demo.todo.grpc.converter;

import com.google.protobuf.Timestamp;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Optional;

@Component
public class LocalDateTimeProtoConverter implements
        ProtoConverter<com.google.protobuf.Timestamp, LocalDateTime> {

    @Override
    public Optional<Timestamp> toProto(LocalDateTime model) {
        if (Objects.isNull(model)) {
            return Optional.empty();
        }
        Instant createdInstant = model.toInstant(ZoneOffset.UTC);
        return Optional.of(Timestamp.newBuilder()
                .setSeconds(createdInstant.getEpochSecond())
                .setNanos(createdInstant.getNano())
                .build());
    }

}
