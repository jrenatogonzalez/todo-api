package dev.rgonzalez.demo.todo.model;

import lombok.Getter;

@Getter
public class PageRequest {
    public static final int DEFAULT_PAGE_SIZE = 20;
    private final int page;
    private final int size;

    private PageRequest(int page, int size) {
        this.page = page;
        this.size = size;
    }

    public static PageRequest of(int page, int size) {
        if (page < 0 || size < 1) {
            throw new IllegalArgumentException("Page cannot be negative and size cannot be less than one");
        }
        return new PageRequest(page, size);
    }

    public static PageRequest firstPage() {
        return new PageRequest(0, DEFAULT_PAGE_SIZE);
    }

    public int getOffset() {
        return page * size;
    }

}
