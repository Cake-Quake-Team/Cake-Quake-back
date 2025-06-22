package com.cakequake.cakequakeback.member.service.auth;

import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.common.utils.JWTUtil;
import com.cakequake.cakequakeback.member.dto.*;
import com.cakequake.cakequakeback.member.dto.auth.*;
import com.cakequake.cakequakeback.member.dto.buyer.BuyerSignupRequestDTO;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.member.entities.MemberRole;
import com.cakequake.cakequakeback.member.entities.MemberStatus;
import com.cakequake.cakequakeback.member.entities.SocialType;
import com.cakequake.cakequakeback.member.repo.MemberRepository;
import com.cakequake.cakequakeback.member.validator.MemberValidator;
import com.cakequake.cakequakeback.security.service.AuthenticatedUserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;


@Service
@Transactional
@Slf4j
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberValidator memberValidator;
    private final JWTUtil jwtUtil;
    private final AuthenticatedUserService authenticatedUserService;

    public MemberServiceImpl(MemberRepository memberRepository, PasswordEncoder passwordEncoder, MemberValidator memberValidator, JWTUtil jwtUtil, AuthenticatedUserService authenticatedUserService) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.memberValidator = memberValidator;
        this.jwtUtil = jwtUtil;
        this.authenticatedUserService = authenticatedUserService;
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

        Optional<Member> optionalMember = memberRepository.findByUserId(userId);
        // 아이디가 없는 경우
        if (optionalMember.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        Member member = optionalMember.get();
        // 탈퇴 회원(Status가 ACTIVE가 아닐 경우)
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.MEMBER_WITHDRAWN);
        }
        // 비밀번호 검증
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        // 로그인 성공 시 토큰에 담을 기본 정보 추출
        Long uid = member.getUid();
        String uname = member.getUname();
        String role = member.getRole().name();

        // 액세스 토큰 생성 (유효기간: 5분)
        String accessToken = jwtUtil.createToken(Map.of("userId", userId, "uname", uname, "role", role), 5);
        // 리프레시 토큰 생성 (유효기간: 7일)
        String refreshToken = jwtUtil.createToken(Map.of("userId", userId, "uname", uname, "role", role), 60 * 24 * 7);

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
            // 전달된 리프레시 토큰을 검증하고 페이로드(claims) 추출
            Claims claims = (Claims) jwtUtil.validateToken(refreshToken);
            // 토큰 내에서 필요한 사용자 정보 추출
            String userId = claims.get("userId", String.class);
            String uname = claims.get("uname", String.class);
            String role = claims.get("role", String.class);
            log.debug("userId: {}", userId);

            // 추출한 사용자 정보로 새로운 액세스 토큰 생성 (유효기간: 5분)
            String newAccessToken = jwtUtil.createToken(Map.of("userId", userId, "uname", uname, "role", role), 5);
            // 새로운 리프레시 토큰 생성 (유효기간: 7일
            String newRefreshToken = jwtUtil.createToken(Map.of("userId", userId, "uname", uname, "role", role), 60 * 24 * 7);

            return new RefreshTokenResponseDTO(newAccessToken, newRefreshToken);
        } catch (Exception e) {
            throw new JwtException(e.getMessage());
        }
    }

    @Override
    public ApiResponseDTO changePassword(PasswordChangeDTO dto) {
        Member member = authenticatedUserService.getCurrentMember();

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(dto.getCurrentPassword(), member.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }
        // 새 비밀번호 정규식 검사
        String newPassword = dto.getNewPassword();
        memberValidator.validatePassword(newPassword);
        // 새 비밀번호로 변경
        member.changePassword(passwordEncoder.encode(newPassword));
        memberRepository.save(member);

        return ApiResponseDTO.builder()
                .success(true)
                .message("비밀번호가 변경되었습니다.")
                .build();
    }


}
