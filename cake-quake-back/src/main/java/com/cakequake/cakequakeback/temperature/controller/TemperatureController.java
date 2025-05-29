package com.cakequake.cakequakeback.temperature.controller;

import com.cakequake.cakequakeback.temperature.dto.TemperatureHistoryResponseDTO;
import com.cakequake.cakequakeback.temperature.dto.TemperatureRequestDTO;
import com.cakequake.cakequakeback.temperature.dto.TemperatureResponseDTO;
import com.cakequake.cakequakeback.temperature.entities.Temperature;
import com.cakequake.cakequakeback.temperature.service.TemperatureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/temperature")

public class TemperatureController {
    private final TemperatureService temperatureService;

    //온도 이력 조회
    @GetMapping("/{uid}/histories")
    public ResponseEntity<List<TemperatureHistoryResponseDTO>> getHistories(@PathVariable Long uid) {
        List<TemperatureHistoryResponseDTO> histories = temperatureService.findHistory(uid);
        return ResponseEntity.ok(histories);

    }

    //온도 조회
    @GetMapping("/{uid}")
    public ResponseEntity<TemperatureResponseDTO> getTemperatureByUid(@PathVariable Long uid) {
        Temperature temperature = temperatureService.getTemperatureByUid(uid);

        TemperatureResponseDTO response = TemperatureResponseDTO.builder()
                .memberId(uid)
                .temperature(temperature.getTemperature())
                .grade(temperature.getGrade())
                .build();

        return ResponseEntity.ok(response);
    }



    //온도 수동 조회(관리자만)
//    @PreAuthorize("hasRole('ADMIN')"))
    @PostMapping("/manual-update")
    public ResponseEntity<String> manualUpdate(@RequestBody TemperatureRequestDTO request) {
        temperatureService.updateByuid(request);
        return ResponseEntity.ok("온도 정보가 정상적으로 업데이트 되었습니다.");
    }


}
