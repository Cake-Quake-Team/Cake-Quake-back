package com.cakequake.cakequakeback.schedule.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Getter

//하나의 날짜+해당 날짜에 포함된 리스트
public class ScheduleCalendarDTO {

    private LocalDate date;
    private List<ScheduleTimeSlotDTO> timeSlots;




}

//private List<ScheduleTimeSlotDTO> timeSlots : 하나의 날짜에 해당하는 예약 시간 슬롯 담기 위한 필드, 예약 캘린더/일정 시각화 API에서 사용
//timeslots : 해당 날짜(date)의 모든 시간대 스케줄,

