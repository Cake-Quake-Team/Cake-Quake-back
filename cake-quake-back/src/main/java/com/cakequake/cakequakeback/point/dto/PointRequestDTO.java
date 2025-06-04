package com.cakequake.cakequakeback.point.dto;

import lombok.*;

@Data
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointRequestDTO {

    //증감할 포인트 양
    private Long amount;

    //이력 기록용 설명
    private String description;
}
