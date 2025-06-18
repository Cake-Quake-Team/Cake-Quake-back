package com.cakequake.cakequakeback.review.dto;

import com.cakequake.cakequakeback.common.entities.BaseEntity;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDeletionRequestDTO  {
    private Long reviewId;
    private Long requestId;
    private String status;
    private String reason;
    private LocalDateTime regDate;

    private String reviewContent;

}
