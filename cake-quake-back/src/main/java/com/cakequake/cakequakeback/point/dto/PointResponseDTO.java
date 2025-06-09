package com.cakequake.cakequakeback.point.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PointResponseDTO {
    private Long uid;

    private Long currentBalance;

}
