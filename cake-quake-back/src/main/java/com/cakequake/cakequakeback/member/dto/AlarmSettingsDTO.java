package com.cakequake.cakequakeback.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlarmSettingsDTO {
    private boolean allAlarm; // 통합 알람 활성화 여부
    //private boolean emailNotification; // 이메일 등 필요시 추가 확장

    public void setAlarm(boolean allAlarm) {
        this.allAlarm = allAlarm;
    }
}
