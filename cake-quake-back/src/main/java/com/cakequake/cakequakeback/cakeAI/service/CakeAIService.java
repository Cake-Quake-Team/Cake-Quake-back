package com.cakequake.cakequakeback.cakeAI.service;

import com.cakequake.cakequakeback.cakeAI.entities.CakeAI;

import java.util.List;

public interface CakeAIService {

    // 간단한 질의응답
    String generateAnswer(String question, String sessionId);

    // 케이크 옵션 추천
    String recommendCakeOptions(String question, String sessionId);

    // 케이크 문구 추천
    String recommendCakeLettering(String question, String sessionId);

    // 케이크 디자인 추천
    String recommendCakeDesign(String question, String sessionId);

    // 채팅 내역 조회
    List<CakeAI> getChatHistory(String sessionId);

}
