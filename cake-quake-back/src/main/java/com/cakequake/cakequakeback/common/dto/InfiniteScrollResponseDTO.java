package com.cakequake.cakequakeback.common.dto;

import lombok.*;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class InfiniteScrollResponseDTO<E> {

    private List<E> content;
    private boolean hasNext;
    private int totalCount;

    // hasNext 계산 추가. Builder 대신 사용 가능.
    public static <E> InfiniteScrollResponseDTO<E> of(List<E> content, int totalCount, PageRequestDTO pageRequestDTO) {
        int page = pageRequestDTO.getPage();
        int size = pageRequestDTO.getSize();
        boolean hasNext = (page * size) < totalCount;
        return new InfiniteScrollResponseDTO<>(content, hasNext, totalCount);
    }
}
