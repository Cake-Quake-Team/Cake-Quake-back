package com.cakequake.cakequakeback.member.service;

import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.common.utils.JWTUtil;
import com.cakequake.cakequakeback.member.dto.*;
import com.cakequake.cakequakeback.member.dto.buyer.BuyerSignupRequestDTO;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.member.entities.MemberRole;
import com.cakequake.cakequakeback.member.entities.SocialType;
import com.cakequake.cakequakeback.member.repo.MemberRepository;
import com.cakequake.cakequakeback.member.validator.MemberValidator;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;


@Service
@Transactional
@Slf4j
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberValidator memberValidator;
    private final JWTUtil jwtUtil;

    public MemberServiceImpl(MemberRepository memberRepository, PasswordEncoder passwordEncoder, MemberValidator memberValidator, JWTUtil jwtUtil) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.memberValidator = memberValidator;
        this.jwtUtil = jwtUtil;
    }

    public ApiResponseDTO signup(BuyerSignupRequestDTO requestDTO) {
        log.debug("---------signup--------------");

        SocialType joinType = SocialType.from(requestDTO.getJoinType());

        /*
        유효성 형식 검사 - userId, 비밀번호, uname 길이, 전화번호 형식, 가입 방식
        중복 검사 - userId, 전화번호
        */
        memberValidator.validateSignupRequest(requestDTO);
        log.debug("---memberValidator 통과---");

        // basic 가입일 때만 비밀번호 인코딩
        String encodedPassword = null;
        switch (joinType) {
            case BASIC:
                encodedPassword = passwordEncoder.encode(requestDTO.getPassword());
                break;

            case KAKAO:
            case GOOGLE:
                // 소셜 회원 가입 처리 로직은 추후 추가 예정
                // 예: 액세스 토큰으로 사용자 정보 조회 → 검증 → 회원 가입
                break;

            default:
                throw new IllegalArgumentException("지원하지 않는 가입 방식입니다."); // 나중에 변경
        }

        /* 휴대폰 인증은 프론트에서 따로 호출 */

        Member member = Member.builder()
                .userId(requestDTO.getUserId())
                .uname(requestDTO.getUname())
                .password(encodedPassword)
                .phoneNumber(requestDTO.getPhoneNumber())
                .publicInfo(requestDTO.getPublicInfo())
                .alarm(requestDTO.getAlarm())
                .role(MemberRole.BUYER)
                .socialType(joinType)
                .build();

        memberRepository.save(member);

        return ApiResponseDTO.builder()
                .success(true)
                .message("회원 가입에 성공하였습니다.")
                .build();
    }

    @Override
    public SigninResponseDTO signin(SigninRequestDTO requestDTO) {

        String userId = requestDTO.getUserId();
        String password = requestDTO.getPassword();

        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        // 비밀번호 검증
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        String uname = member.getUname();
        String role = member.getRole().name();

        String accessToken = jwtUtil.createToken(Map.of("userId", userId, "uname", uname, "role", role), 5);
        String refreshToken = jwtUtil.createToken(Map.of("userId", userId, "uname", uname, "role", role), 60 * 24 * 7); // 7일

        return SigninResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(member.getUserId())
                .uname(member.getUname())
                .role(member.getRole().name())
                .build();
    }

    @Override
    public RefreshTokenResponseDTO refreshTokens(String accessToken, RefreshTokenRequestDTO requestDTO) {
        log.debug("---MemberServiceImpl---refreshTokens()---");
        String refreshToken = requestDTO.getRefreshToken();

        try {
            Claims claims = (Claims) jwtUtil.validateToken(refreshToken);
            String userId = claims.get("userId", String.class);
            String uname = claims.get("uname", String.class);
            String role = claims.get("role", String.class);
            log.debug("userId: {}", userId);

            String newAccessToken = jwtUtil.createToken(Map.of("userId", userId, "uname", uname, "role", role), 5);
            String newRefreshToken = jwtUtil.createToken(Map.of("userId", userId, "uname", uname, "role", role), 60 * 24 * 7); // 7일

            return new RefreshTokenResponseDTO(newAccessToken, newRefreshToken);
        } catch (Exception e) {
            throw new JwtException(e.getMessage());
        }

    }


}
