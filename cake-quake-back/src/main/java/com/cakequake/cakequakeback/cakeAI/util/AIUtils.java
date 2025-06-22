package com.cakequake.cakequakeback.cakeAI.util;

import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;

public class AIUtils {

    public static <T> T handleAIProcessing(AIWork<T> aiWork) {
        try {
            return aiWork.execute();
        } catch (IllegalArgumentException e) {
            // 예: 프롬프트 템플릿 파라미터 누락 등
            throw new BusinessException(ErrorCode.PROMPT_TEMPLATE_ERROR);
        } catch (RuntimeException e) {
            // 예: 외부 API 호출 실패
            if (e.getMessage() != null && e.getMessage().contains("OpenAI")) {
                throw new BusinessException(ErrorCode.OPENAI_API_ERROR);
            }
            throw new BusinessException(ErrorCode.AI_PROCESSING_FAILED);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.AI_PROCESSING_FAILED);
        }
    }
}
