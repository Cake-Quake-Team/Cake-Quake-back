package com.cakequake.cakequakeback.badge.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepresentativeBadgeResponseDTO {
    private String icon;
    private String name;
}