package com.lipa.application.dto;

import java.util.List;

public final class PageResult<T> {

    private final int limit;
    private final int offset;
    private final int returned;
    private final int nextOffset;
    private final List<T> items;

    private PageResult(int limit, int offset, List<T> items) {
        this.limit = limit;
        this.offset = offset;
        this.items = items;
        this.returned = items.size();
        this.nextOffset = offset + returned;
    }

    public static <T> PageResult<T> of(PageRequest page, List<T> items) {
        return new PageResult<>(page.limit(), page.offset(), items);
    }

    public int limit() {
        return limit;
    }

    public int offset() {
        return offset;
    }

    public int returned() {
        return returned;
    }

    public int nextOffset() {
        return nextOffset;
    }

    public List<T> items() {
        return items;
    }
}
