package com.cakequake.cakequakeback.qna.dto;

import com.cakequake.cakequakeback.qna.entities.QnAStatus;
import com.cakequake.cakequakeback.qna.entities.QnAType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QnAResponseDTO {
    private Long qnaId;
    private Long memberId;
    private QnAType qnAType;
    private String title;
    private String content;
    private String adminResponse;
    private QnAStatus status;
    private LocalDateTime regDate;
    private LocalDateTime modDate;

}
