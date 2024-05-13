package dev.rgonzalez.demo.todo.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PageRequestTest {

    @Test
    void shouldCreateFirstPageRequest() {
        // When
        PageRequest pageRequest = PageRequest.firstPage();

        // Then
        assertThat(pageRequest).isNotNull();
        assertThat(pageRequest.getPage()).isZero();
        assertThat(pageRequest.getSize()).isEqualTo(20);
        assertThat(pageRequest.getOffset()).isZero();
    }

    @Test
    void shouldCreateACustomPageRequest() {
        // When
        PageRequest pageRequest = PageRequest.of(2, 10);

        // Then
        assertThat(pageRequest).isNotNull();
        assertThat(pageRequest.getPage()).isEqualTo(2);
        assertThat(pageRequest.getSize()).isEqualTo(10);
        assertThat(pageRequest.getOffset()).isEqualTo(20);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenPageIsNegative() {
        // When & Then
        assertThatThrownBy(() -> PageRequest.of(-1, 10),
                "Page cannot be negative and size cannot be less than one")
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenPageSizeIsLessThanOne() {
        // When & Then
        assertThatThrownBy(() -> PageRequest.of(3, 0),
                "Page cannot be negative and size cannot be less than one")
                .isInstanceOf(IllegalArgumentException.class);
    }
}