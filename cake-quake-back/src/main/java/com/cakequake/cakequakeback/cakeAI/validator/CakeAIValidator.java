package com.cakequake.cakequakeback.cakeAI.validator;

import com.cakequake.cakequakeback.cakeAI.dto.AIRequestDTO;
import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class CakeAIValidator {

    // AI 공통 유효성 검사
    public void validateCommonAI(AIRequestDTO aiRequestDTO) {
        if (aiRequestDTO == null || !StringUtils.hasText(aiRequestDTO.getQuestion())) {
            throw new BusinessException(ErrorCode.INVALID_AI_REQUEST);
        }

        if (aiRequestDTO.getQuestion().length() > 300) {
            throw new BusinessException(ErrorCode.INVALID_AI_REQUEST);
        }
    }
}
