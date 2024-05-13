package dev.rgonzalez.demo.todo.model;

import lombok.Getter;

import java.util.List;

@Getter
public class PagedResult<T> {
    private final List<T> content;
    private final int totalElements;
    private final int totalPages;
    private final int size;
    private final int page;

    public PagedResult(List<T> content, int totalElements, PageRequest pageRequest) {
        this.content = content;
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil(totalElements / (double) pageRequest.getSize());
        this.size = pageRequest.getSize();
        this.page = pageRequest.getPage();
    }

}
