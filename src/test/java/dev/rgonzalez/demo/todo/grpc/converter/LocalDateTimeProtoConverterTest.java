package dev.rgonzalez.demo.todo.grpc.converter;

import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class LocalDateTimeProtoConverterTest {
    private LocalDateTimeProtoConverter converter;

    @BeforeEach
    void setUp() {
        this.converter = new LocalDateTimeProtoConverter();
    }

    @Test
    void shouldReturnOptionalEmptyIfLocalDateTimeIsNull() {
        // When
        Optional<Timestamp> result = converter.toProto(null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldConvertTimeStampProto() {
        // Given
        LocalDateTime dateTime = LocalDateTime.of(2024, 5, 19, 8, 34, 23);

        // When
        Optional<Timestamp> result = converter.toProto(dateTime);

        // Then
        assertThat(result).isPresent();
        Timestamp timestamp = result.get();
        LocalDateTime dateTimeResult = Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos())
                .atZone(ZoneOffset.UTC)
                .toLocalDateTime();
        assertThat(dateTimeResult).isEqualTo(dateTime);
    }

}