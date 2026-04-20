package com.shopx.product.core;

import lombok.*;

import java.util.List;
import java.util.function.Function;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PageResponse<T> {
    private List<T> data;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;

    public <R> PageResponse<R> map(Function<T, R> mapper) {

        List<R> mappedData = this.data.stream()
                .map(mapper)
                .toList();

        return PageResponse.<R>builder()
                .data(mappedData)
                .page(this.page)
                .size(this.size)
                .totalElements(this.totalElements)
                .totalPages(this.totalPages)
                .hasNext(this.hasNext)
                .hasPrevious(this.hasPrevious)
                .build();
    }
}