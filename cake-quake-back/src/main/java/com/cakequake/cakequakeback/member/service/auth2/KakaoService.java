package com.cakequake.cakequakeback.member.service.auth2;

import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.member.dto.auth2.KakaoUserDTO;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.LinkedHashMap;

// 카카오 유저 정보 획득
@Service
@Log4j2
public class KakaoService {

    @Value("${kakao.api.user.info.url}")
    private String kakaoGetUserURL;

    public KakaoUserDTO getKakaoUserInfo(String accessToken) {
        log.debug("------KakaoService--------getKakaoUserInfo--------");

        if(accessToken == null){
            throw new BusinessException(ErrorCode.MISSING_JWT);
        }
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-Type","application/x-www-form-urlencoded");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        UriComponents uriBuilder = UriComponentsBuilder.fromUriString(kakaoGetUserURL).build();

        // 유저 정보를 얻어오는 url로 헤더에 토큰을 담아 요청
        ResponseEntity<LinkedHashMap> response = restTemplate.exchange(
                uriBuilder.toString(),
                HttpMethod.GET,
                entity,
                LinkedHashMap.class
        );
//        log.debug(response);

        // 응답 받은 데이터에서 필요한 정보를 추출
        LinkedHashMap<String, LinkedHashMap> bodyMap = response.getBody();
//        log.debug("bodyMap: {}", bodyMap);

        LinkedHashMap<String, Object> kakaoAccount = bodyMap.get("kakao_account");
//        log.debug("kakaoAccount: {}", kakaoAccount);
        LinkedHashMap<String, Object> profile = (LinkedHashMap<String, Object>) kakaoAccount.get("profile");

        String email = (String) kakaoAccount.get("email");
        String nickname = (String) profile.get("nickname");

        return KakaoUserDTO.builder()
                .email(email)
                .nickname(nickname)
                .build();
    }
}
