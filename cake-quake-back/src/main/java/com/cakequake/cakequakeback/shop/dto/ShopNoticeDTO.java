package com.cakequake.cakequakeback.shop.dto;

import jakarta.persistence.Entity;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Builder
@Getter
@AllArgsConstructor


public class ShopNoticeDTO {
    private String title;
    private String content;


}
