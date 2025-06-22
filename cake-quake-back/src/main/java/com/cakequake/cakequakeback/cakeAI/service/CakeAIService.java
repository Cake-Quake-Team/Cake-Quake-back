package com.cakequake.cakequakeback.cakeAI.service;

public interface CakeAIService {

    // 간단한 질의응답
    String generateAnswer(String question);

    // 케이크 옵션 추천
    String recommendCakeOptions(String question);

    // 케이크 문구 추천
    String recommendCakeLettering(String question);

    // 케이크 디자인 추천
    String recommendCakeDesign(String question);

}
