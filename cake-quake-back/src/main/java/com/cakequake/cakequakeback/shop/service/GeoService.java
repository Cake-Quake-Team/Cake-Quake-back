package com.cakequake.cakequakeback.shop.service;

import com.cakequake.cakequakeback.shop.dto.KakaoAddressResponse;
import com.cakequake.cakequakeback.shop.dto.Point;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

@Service
public class GeoService {

    // application.yml에서 설정한 카카오 REST API 키를 읽어옴
    @Value("${spring.pg.kakao.map-key}")
    private String kakaoApiKey;

    // 외부 HTTP 요청을 위한 RestTemplate 객체 생성
    private final RestTemplate restTemplate = new RestTemplate();

    // 위도 경도 변환하여 point 객체로 반환
    public Point getCoordinatesFromAddress(String address) {
        // 주소 문자열을 URL 인코딩 (한글 등 특수문자 포함 대비)
        String encodedAddress = UriUtils.encode(address, StandardCharsets.UTF_8);

        // 카카오 주소 검색 API 요청 URL 구성
        String url = "https://dapi.kakao.com/v2/local/search/address.json?query=" + encodedAddress;

        // 요청 헤더 설정 (인증 키 포함)
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoApiKey);

        // 요청 엔티티 생성 (GET 요청이므로 바디는 없음)
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // 카카오 API 호출 (주소 → 좌표)
        ResponseEntity<KakaoAddressResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                KakaoAddressResponse.class
        );

        // 응답 Body 파싱
        KakaoAddressResponse kakaoResponse = response.getBody();

        // 주소 결과가 없거나 응답이 null인 경우 예외 처리
        if (kakaoResponse == null || kakaoResponse.documents.isEmpty()) {
            throw new RuntimeException("좌표 정보를 찾을 수 없습니다.");
        }

        // 첫 번째 검색 결과에서 위도(y), 경도(x) 값을 추출
        KakaoAddressResponse.Document doc = kakaoResponse.documents.get(0);

        return new Point(Double.parseDouble(doc.y), Double.parseDouble(doc.x)); // 위도, 경도
    }
}
