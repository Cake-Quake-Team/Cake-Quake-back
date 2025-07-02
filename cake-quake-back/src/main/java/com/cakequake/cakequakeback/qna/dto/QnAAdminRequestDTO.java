package com.cakequake.cakequakeback.qna.dto;


import com.cakequake.cakequakeback.qna.entities.QnAType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QnAAdminRequestDTO {
    @NotNull
    private Long qnaId;

    @NotNull
    private String adminResponse;

}
