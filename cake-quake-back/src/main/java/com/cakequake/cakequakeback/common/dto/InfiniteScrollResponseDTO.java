package com.cakequake.cakequakeback.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InfiniteScrollResponseDTO<E> {

    private List<E> content;
    private boolean hasNext;
    private int totalCount;

}
