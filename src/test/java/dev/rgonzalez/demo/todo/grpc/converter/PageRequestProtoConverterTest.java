package dev.rgonzalez.demo.todo.grpc.converter;

import dev.rgonzalez.demo.todo.model.PageRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PageRequestProtoConverterTest {
    private PageRequestProtoConverter pageRequestProtoConverter;

    @BeforeEach
    void setUp() {
        pageRequestProtoConverter = new PageRequestProtoConverter();
    }

    @Test
    void shouldConvertToModel() {
        // Given
        var pageRequestProto = dev.rgonzalez.proto.todo.common.messages.PageRequest.newBuilder()
                .setPage(3)
                .setSize(10)
                .build();

        // When
        Optional<PageRequest> result = pageRequestProtoConverter.toModel(pageRequestProto);

        // Then
        assertThat(result).isPresent();
        PageRequest actualPageRequest = result.get();
        assertThat(actualPageRequest.getPage()).isEqualTo(3);
        assertThat(actualPageRequest.getSize()).isEqualTo(10);
    }

    @Test
    void shouldConvertToProto() {
        // Given
        var pageRequestModel = PageRequest.of(4, 20);

        // When
        var result = pageRequestProtoConverter.toProto(pageRequestModel);

        // Then
        assertThat(result).isPresent();
        var actualPageRequestProto = result.get();
        assertThat(actualPageRequestProto.getPage()).isEqualTo(4);
        assertThat(actualPageRequestProto.getSize()).isEqualTo(20);
    }

}