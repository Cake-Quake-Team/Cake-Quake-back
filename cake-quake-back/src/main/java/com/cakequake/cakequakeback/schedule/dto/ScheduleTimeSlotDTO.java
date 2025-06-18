package com.cakequake.cakequakeback.schedule.dto;

import com.cakequake.cakequakeback.schedule.entities.ReservationStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Getter

//단일 시간대 슬롯 정보(예약 여부, 시간,예약  개수 포함)
public class ScheduleTimeSlotDTO {
    private String timeSlot;
    private LocalTime startTime;
    private LocalTime endTime;




}
