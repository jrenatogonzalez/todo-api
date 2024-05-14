package dev.rgonzalez.demo.todo.grpc.converter;

import dev.rgonzalez.demo.todo.model.Priority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.stream.Stream;

import static dev.rgonzalez.proto.todo.common.messages.Priority.PRIORITY_HIGH;
import static dev.rgonzalez.proto.todo.common.messages.Priority.PRIORITY_LOW;
import static dev.rgonzalez.proto.todo.common.messages.Priority.PRIORITY_MEDIUM;
import static dev.rgonzalez.proto.todo.common.messages.Priority.PRIORITY_UNSPECIFIED;
import static org.assertj.core.api.Assertions.assertThat;

class PriorityProtoConverterTest {
    private PriorityProtoConverter priorityProtoConverter;

    @BeforeEach
    void setUp() {
        priorityProtoConverter = new PriorityProtoConverter();
    }

    @Test
    void shouldConvertUnspecifiedProtoToOptionalEmpty() {
        // When
        Optional<Priority> result = priorityProtoConverter.toModel(PRIORITY_UNSPECIFIED);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldConvertNullToUnspecifiedProto() {
        // When
        var result = priorityProtoConverter.toProto(null);

        // Then
        assertThat(result).isEqualTo(PRIORITY_UNSPECIFIED);
    }

    @ParameterizedTest
    @MethodSource("protoToModelTestData")
    void shouldConvertToModel(dev.rgonzalez.proto.todo.common.messages.Priority proto, Priority expectedModel) {
        // When
        var result = priorityProtoConverter.toModel(proto);

        // Then
        assertThat(result).isPresent();
        Priority actualModel = result.get();
        assertThat(actualModel).isEqualTo(expectedModel);
    }

    @ParameterizedTest
    @MethodSource("modelToProtoTestData")
    void shouldConvertToProto(Priority model, dev.rgonzalez.proto.todo.common.messages.Priority expectedProto) {
        // When
        var result = priorityProtoConverter.toProto(model);

        // Then
        assertThat(result)
                .isNotNull()
                .isEqualTo(expectedProto);
    }

    private static Stream<Arguments> protoToModelTestData() {
        return Stream.of(
                Arguments.of(PRIORITY_LOW, Priority.LOW),
                Arguments.of(PRIORITY_MEDIUM, Priority.MEDIUM),
                Arguments.of(PRIORITY_HIGH, Priority.HIGH)
        );
    }

    private static Stream<Arguments> modelToProtoTestData() {
        return Stream.of(
                Arguments.of(Priority.LOW, PRIORITY_LOW),
                Arguments.of(Priority.MEDIUM, PRIORITY_MEDIUM),
                Arguments.of(Priority.HIGH, PRIORITY_HIGH)
        );
    }

}