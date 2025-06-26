package com.cakequake.cakequakeback.cakeAI.controller;


import com.cakequake.cakequakeback.cakeAI.dto.AIRequestDTO;
import com.cakequake.cakequakeback.cakeAI.service.CakeAIService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class CakeAIController {

    private final CakeAIService cakeAIService;

    // 간단한 질의응답
    @PostMapping("/chat")
    public ResponseEntity<String> generateAnswer(@RequestBody AIRequestDTO request) {
        String answer = cakeAIService.generateAnswer(request.getQuestion());
        return ResponseEntity.ok(answer);
    }

    // 케이크 옵션 추천 요청
    @PostMapping("/recommend/options")
    public ResponseEntity<String> recommendCakeOptions(@RequestBody AIRequestDTO request) {
        String answer = cakeAIService.recommendCakeOptions(request.getQuestion());
        return ResponseEntity.ok(answer);
    }

    // 케이크 문구 추천 요청
    @PostMapping("/recommend/lettering")
    public ResponseEntity<String> recommendCakeLettering(@RequestBody AIRequestDTO request) {
        String answer = cakeAIService.recommendCakeLettering(request.getQuestion());
        return ResponseEntity.ok(answer);
    }

    // 케이크 디자인 추천 요청
    @PostMapping("/recommend/image")
    public ResponseEntity<Map<String, String>> recommendCakeImage(@RequestBody AIRequestDTO request) {
        String imageUrl = cakeAIService.recommendCakeDesign(request.getQuestion());
        return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
    }
}

