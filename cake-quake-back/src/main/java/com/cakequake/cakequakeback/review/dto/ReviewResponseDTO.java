package com.cakequake.cakequakeback.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDTO {
    private Long reviewId;
    private Long orderId;
    private Long cakeId;
    private String cname;
    private Integer rating;
    private String content;

    private String reviewPictureUrl;
    private LocalDateTime regDate;
    private LocalDateTime modDate;
    private String reply;
}
