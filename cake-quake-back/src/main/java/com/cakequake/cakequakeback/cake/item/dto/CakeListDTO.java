package com.cakequake.cakequakeback.cake.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
// 케이크 상품 목록 조회 DTO
public class CakeListDTO {
    private Long cakeId;
    private String cname;
    private int price;
    private String thumbnailImageUrl;
}
