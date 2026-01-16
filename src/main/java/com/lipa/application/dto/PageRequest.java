package com.lipa.application.dto;

public final class PageRequest {

    public static final int DEFAULT_LIMIT = 20;
    public static final int MAX_LIMIT = 200;

    private final int limit;
    private final int offset;

    private PageRequest(int limit, int offset) {
        this.limit = limit;
        this.offset = offset;
    }

    public static PageRequest of(int limit, int offset) {
        int safeLimit = limit <= 0 ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);
        int safeOffset = Math.max(offset, 0);
        return new PageRequest(safeLimit, safeOffset);
    }

    public int limit() {
        return limit;
    }

    public int offset() {
        return offset;
    }
}
