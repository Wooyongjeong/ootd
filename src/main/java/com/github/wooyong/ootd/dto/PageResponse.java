package com.github.wooyong.ootd.dto;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 * 공통 페이징 응답 DTO입니다.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    /**
     * Spring Data Page를 API 응답 형식으로 변환합니다.
     */
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }
}
