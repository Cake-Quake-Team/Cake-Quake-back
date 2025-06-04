package com.cakequake.cakequakeback.point.dto;

import com.cakequake.cakequakeback.point.entities.ChangeType;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PointHistoryResponseDTO {

    private Long pointHistoryId;

    private Long uid;

    private ChangeType changeType;

    private String description;

    private Long amount;

    private Long balanceAmount;

    private LocalDateTime regDate;
}
