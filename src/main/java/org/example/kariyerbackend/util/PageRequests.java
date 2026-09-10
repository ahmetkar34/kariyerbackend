package org.example.kariyerbackend.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PageRequests {

    private static final int MAX_SIZE = 100;

    private PageRequests() {
    }

    public static Pageable of(int page, int size, String sortField) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_SIZE);
        return PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, sortField));
    }
}
