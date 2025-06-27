package com.cakequake.cakequakeback.shop.dto;

import com.cakequake.cakequakeback.cake.item.dto.ImageDTO;
import com.cakequake.cakequakeback.shop.entities.ShopStatus;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString

public class ShopUpdateDTO {

    private String address; //not null
    private String phone;
    private String content; //not null
    private LocalTime openTime;//not
    private LocalTime closeTime; //not
    private String closeDays;
    private String websiteUrl;
    private String instagramUrl;
    private ShopStatus status;
    private String thumbnailImageUrl;
    private List<Long> imageIds;
    private Long thumbnailImageId;





}
