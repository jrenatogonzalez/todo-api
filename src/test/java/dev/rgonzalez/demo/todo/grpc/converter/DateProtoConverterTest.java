package dev.rgonzalez.demo.todo.grpc.converter;

import dev.rgonzalez.proto.todo.common.messages.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DateProtoConverterTest {
    private DateProtoConverter converter;

    @BeforeEach
    void setUp() {
        converter = new DateProtoConverter();
    }

    @Test
    void shouldConvertToProto() {
        // Given
        LocalDate dueDate = LocalDate.of(2024,6,3);

        // When
        Optional<Date> result = converter.toProto(dueDate);

        // Then
        assertThat(result).isPresent();
        Date actualDate = result.get();
        assertThat(actualDate.getYear()).isEqualTo(2024);
        assertThat(actualDate.getMonth()).isEqualTo(6);
        assertThat(actualDate.getDay()).isEqualTo(3);
    }

    @Test
    void shouldConvertFromModelToOptionalEmpty() {
        // When
        Optional<Date> result = converter.toProto(null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldConvertToModel() {
        // Given
        Date dueDate = Date.newBuilder().setYear(2024).setMonth(5).setDay(12).build();

        // When
        Optional<LocalDate> result = converter.toModel(dueDate);

        // Then
        assertThat(result).isPresent();
        LocalDate actualDate = result.get();
        assertThat(actualDate.getYear()).isEqualTo(2024);
        assertThat(actualDate.getMonthValue()).isEqualTo(5);
        assertThat(actualDate.getDayOfMonth()).isEqualTo(12);
    }

    @Test
    void shouldConvertFromProtoToOptional() {
        // When
        Optional<LocalDate> result = converter.toModel(null);

        // Then
        assertThat(result).isEmpty();
    }

}