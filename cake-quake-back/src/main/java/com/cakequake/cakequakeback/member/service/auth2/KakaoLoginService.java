package com.cakequake.cakequakeback.member.service.auth2;

import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.common.utils.JWTUtil;
import com.cakequake.cakequakeback.member.dto.ApiResponseDTO;
import com.cakequake.cakequakeback.member.dto.auth2.KakaoSignupInitDTO;
import com.cakequake.cakequakeback.member.dto.auth2.KakaoUserDTO;
import com.cakequake.cakequakeback.member.dto.auth2.SocialSigninResponseDTO;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.member.entities.MemberStatus;
import com.cakequake.cakequakeback.member.repo.MemberRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

//유저 판단, 로그인, 회원가입 분기 등
@Service
@Log4j2
public class KakaoLoginService {

    private final KakaoService kakaoService;
    private final MemberRepository memberRepository;
    private final JWTUtil jwtUtil;

    public KakaoLoginService(KakaoService kakaoService, MemberRepository memberRepository, JWTUtil jwtUtil) {
        this.kakaoService = kakaoService;
        this.memberRepository = memberRepository;
        this.jwtUtil = jwtUtil;
    }

    public ApiResponseDTO processKakaoLogin(String accessToken) {
        // 카카오 유저의 이메일과 닉네임 획득
        KakaoUserDTO kakaoUserDTO = kakaoService.getKakaoUserInfo(accessToken);
//        log.debug("---processKakaoLogin---accessToken: {}", accessToken.substring(0, 10));

        String email = kakaoUserDTO.getEmail();

        // 이미 가입한 카카오 유저인지 확인
        Optional<Member> optionalMember = memberRepository.findByUserId(email);
//        log.debug("---processKakaoLogin---optionalMember: {}", optionalMember);

        if (optionalMember.isPresent()) {
            // 기존 유저 - 로그인 처리
            Member member = optionalMember.get();
//            log.debug("---processKakaoLogin---member: {}", member);

            // 유저 상태 체크
            if (member.getStatus() != MemberStatus.ACTIVE) {
                throw new BusinessException(ErrorCode.MEMBER_WITHDRAWN);
            }

            /* 토큰 발급 */
            String userId = member.getUserId();
            String uname = member.getUname();
            String role = member.getRole().name();

            // 토큰에 정보 추가
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", userId);
            claims.put("uname", uname);
            claims.put("role", role);

            String jwtAccessToken = jwtUtil.createToken(claims, 5);       // 5분
            String jwtRefreshToken = jwtUtil.createToken(claims, 60 * 24 * 7); // 7일

            SocialSigninResponseDTO signinResponseDTO = SocialSigninResponseDTO.builder()
                    .exists(true) // 가입 한 상태
                    .accessToken(jwtAccessToken)
                    .refreshToken(jwtRefreshToken)
                    .userId(member.getUserId())
                    .uname(member.getUname())
                    .role(role)
                    .build();

            return ApiResponseDTO.builder()
                    .success(true)
                    .message("카카오 유저 로그인 성공")
                    .data(signinResponseDTO)
                    .build();
        } else {
            // 신규 유저 - 회원가입 유도
            KakaoSignupInitDTO dto = KakaoSignupInitDTO.builder()
                    .exists(false) // 가입 안 한 상태
                    .email(kakaoUserDTO.getEmail())
                    .nickname(kakaoUserDTO.getNickname())
                    .build();
            log.debug("---processKakaoLogin---신규 가입---KakaoSignupInitDTO: {}", dto);

            return ApiResponseDTO.builder()
                    .success(true)
                    .message("회원 가입이 필요한 신규 카카오 유저")
                    .data(dto)
                    .build();

        } // end if~else

    }

}
