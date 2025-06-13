package com.cakequake.cakequakeback.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequestDTO {

    private Long orderId;

    private Long cakeId;

    @NotNull(message = "평점(rating)은 필수입니다.")
    @Min(value = 0 , message = "평점은 최소 0점 이상입니다.")
    @Max(value = 5, message = "평점은 최대 5점 이하입니다.")
    private Integer rating;

    @NotBlank(message = "내용은 필수 입니다.")
    private String content;


    private MultipartFile reviewPictureUrl;

}
