package com.cakequake.cakequakeback.cakeAI.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class AIRequestDTO {
    private String question; // 예: "귀여운 느낌의 케이크 추천해줘"
}

