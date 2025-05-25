package com.cakequake.cakequakeback.review.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReplyRequestDTO {
    @NotBlank(message="답글 내용을 입력하세요.")
    private String reply;
}
