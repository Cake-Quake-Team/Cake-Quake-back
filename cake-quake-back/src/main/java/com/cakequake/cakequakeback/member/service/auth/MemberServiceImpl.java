package com.cakequake.cakequakeback.member.service.auth;

import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.common.utils.CookieUtil;
import com.cakequake.cakequakeback.common.utils.JWTUtil;
import com.cakequake.cakequakeback.member.dto.*;
import com.cakequake.cakequakeback.member.dto.auth.*;
import com.cakequake.cakequakeback.member.dto.auth2.SocialSignupRequestDTO;
import com.cakequake.cakequakeback.member.dto.buyer.BuyerSignupRequestDTO;
import com.cakequake.cakequakeback.member.entities.*;
import com.cakequake.cakequakeback.member.repo.MemberDetailRepository;
import com.cakequake.cakequakeback.member.repo.MemberRepository;
import com.cakequake.cakequakeback.member.validator.MemberValidator;
import com.cakequake.cakequakeback.point.service.PointService;
import com.cakequake.cakequakeback.security.jwt.JWTClaimProvider;
import com.cakequake.cakequakeback.security.service.AuthenticatedUserService;
import com.cakequake.cakequakeback.shop.repo.ShopRepository;
import com.cakequake.cakequakeback.temperature.service.TemperatureService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;


@Service
@Transactional
@Slf4j
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final ShopRepository shopRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberValidator memberValidator;
    private final JWTUtil jwtUtil;
    private final JWTClaimProvider jwtClaimProvider;
    private final AuthenticatedUserService authenticatedUserService;
    private final TemperatureService temperatureService;
    private final PointService pointService;
    private final MemberDetailRepository memberDetailRepository;

    public MemberServiceImpl(
            MemberRepository memberRepository,
            ShopRepository shopRepository,
            PasswordEncoder passwordEncoder,
            MemberValidator memberValidator,
            JWTUtil jwtUtil,
            JWTClaimProvider jwtClaimProvider,
            AuthenticatedUserService authenticatedUserService,
            TemperatureService temperatureService,
            PointService pointService, MemberDetailRepository memberDetailRepository
    ) {
        this.memberRepository = memberRepository;
        this.shopRepository = shopRepository;
        this.passwordEncoder = passwordEncoder;
        this.memberValidator = memberValidator;
        this.jwtUtil = jwtUtil;
        this.jwtClaimProvider = jwtClaimProvider;
        this.authenticatedUserService = authenticatedUserService;
        this.temperatureService = temperatureService;
        this.pointService = pointService;
        this.memberDetailRepository = memberDetailRepository;
    }

    /*
        25.06.30 수정
        소셜 가입은 로직 분리.
     */
    public ApiResponseDTO signup(BuyerSignupRequestDTO requestDTO) {
//        log.debug("---------signup--------------");

        SocialType joinType = SocialType.from(requestDTO.getJoinType());

        /*
            유효성 형식 검사 - userId, 비밀번호, uname 길이, 전화번호 형식, 가입 방식
            중복 검사 - userId, 전화번호
        */
        memberValidator.validateSignupRequest(requestDTO);
        log.debug("---memberValidator 통과---");

        String encodedPassword = passwordEncoder.encode(requestDTO.getPassword());

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

        MemberDetail detail = MemberDetail.builder()
                .member(member)
                .profileBadge("")
                .build();

        Member savedMember = memberRepository.save(member);
        memberDetailRepository.save(detail);
        // 첫 온도 설정
        temperatureService.createInitialTemperature(savedMember);

        // ★ 회원가입 축하 포인트 3000 적립 ★
        pointService.changePoint(
                savedMember.getUid(),          // 회원 PK
                3000L,                              // 적립할 포인트
                "회원가입 축하 3,000포인트"          // 적립 사유
        );

        return ApiResponseDTO.builder()
                .success(true)
                .message("회원 가입에 성공하였습니다.")
                .build();
    }

    @Override
    public SigninResponseDTO signupSocial(SocialSignupRequestDTO requestDTO) {
//        log.debug("---------signupSocial--------------");

        SocialType joinType = SocialType.from(requestDTO.getJoinType());
        // basic 이면 안 됨
        if (joinType == SocialType.BASIC) throw new BusinessException(ErrorCode.INVALID_SIGNUP_TYPE);
        // 소셜 아이디가 null 이면
        if(requestDTO.getUserId() == null) throw new BusinessException(ErrorCode.MISSING_SOCIAL_ID);

        // 카카오에서 받아오는 닉네임 -> uname 정제 (특수문자 제거 및 길이 제한)
        String nickname = requestDTO.getUname();
        if (nickname != null) {
            // 특수문자 제거: 문자/숫자/공백만 허용
            nickname = nickname.replaceAll("[^\\p{L}\\p{N}]", "");
            // 최대 20자 제한
            if (nickname.length() > 20) {
                nickname = nickname.substring(0, 20);
            }
        } // end if
        requestDTO.changeUname(nickname);

        /*
            유효성 형식 검사 - uname 길이, 전화번호 형식, 가입 방식
            중복 검사 - userId, 전화번호
        */
        memberValidator.validateSocialSignupRequest(requestDTO);
        log.debug("---memberValidator 통과---");

        String encodedPassword = passwordEncoder.encode(UUID.randomUUID().toString());

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

        Member savedMember = memberRepository.save(member);
//        log.debug("소셜 회원가입 완료 - uid: {}, userId: {}", savedMember.getUid(), savedMember.getUserId());
        // 첫 온도 설정
        temperatureService.createInitialTemperature(savedMember);

        // ★ 회원가입 축하 포인트 3000 적립 ★
        pointService.changePoint(
                savedMember.getUid(),          // 회원 PK
                3000L,                              // 적립할 포인트
                "회원가입 축하 3,000포인트"          // 적립 사유
        );

        // 회원 가입 후 자동 로그인을 위해 토큰 발급.
        Map<String, Object> claims = jwtClaimProvider.createClaims(member);

        // 액세스 토큰 생성 (유효기간: 5분)
        String accessToken = jwtUtil.createToken(claims, 5);
        // 리프레시 토큰 생성 (유효기간: 7일)
        String refreshToken = jwtUtil.createToken(claims, 60 * 24 * 7);

        return SigninResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(member.getUserId())
                .uname(member.getUname())
                .role(member.getRole().name())
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

        // 토큰에 저장된 정보들
        Map<String, Object> claims = jwtClaimProvider.createClaims(member);

        // 액세스 토큰 생성 (유효기간: 5분)
        String accessToken = jwtUtil.createToken(claims, 5);
        // 리프레시 토큰 생성 (유효기간: 7일)
        String refreshToken = jwtUtil.createToken(claims, 60 * 24 * 7);

        return SigninResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(member.getUserId())
                .uname(member.getUname())
                .role(member.getRole().name())
                .build();
    }

//    @Override
//    public RefreshTokenResponseDTO refreshTokens(String accessToken, RefreshTokenRequestDTO requestDTO) {
//        log.debug("---MemberServiceImpl---refreshTokens()---");
//        String refreshToken = requestDTO.getRefreshToken();
//
//        try {
//            // 전달된 리프레시 토큰을 검증하고 페이로드(claims) 추출
//            Claims claims = (Claims) jwtUtil.validateToken(refreshToken);
//            // 토큰 내에서 필요한 사용자 정보 추출
//            Long uid = claims.get("uid", Long.class);
//            log.debug("---refreshTokens---uid: {}", uid);
//            String userId = claims.get("userId", String.class);
//            String uname = claims.get("uname", String.class);
//            String role = claims.get("role", String.class);
//            Long shopId = claims.get("shopId", Long.class);
//            log.debug("userId: {}", userId);
//
//            // 토큰에 정보 추가
//            Map<String, Object> tokenClaims = new HashMap<>();
//            tokenClaims.put("uid", uid);
//            tokenClaims.put("userId", userId);
//            tokenClaims.put("uname", uname);
//            tokenClaims.put("role", role);
//
//            // shopId가 있는 경우에만 추가
//            if (shopId != null) {
//                tokenClaims.put("shopId", shopId);
//            }
//
//            // 추출한 사용자 정보로 새로운 액세스 토큰 생성 (유효기간: 5분)
//            String newAccessToken = jwtUtil.createToken(tokenClaims, 5);
//            // 새로운 리프레시 토큰 생성 (유효기간: 7일
//            String newRefreshToken = jwtUtil.createToken(tokenClaims, 60 * 24 * 7);
//
//            return new RefreshTokenResponseDTO(newAccessToken, newRefreshToken);
//        } catch (Exception e) {
//            throw new JwtException(e.getMessage());
//        }
//    }
    /*
        25.07.02 리액트 쿠키에서 HTTPOnly 쿠키로 변경. 새로 생성한 CookieUtil 이용.
    */
    @Override
    public Map<String, Object> refreshTokens(HttpServletRequest request, HttpServletResponse response) {
        log.debug("---MemberServiceImpl---refreshTokens()---");

        String refreshToken = CookieUtil.getCookieValue(request, "refreshToken");

        try {
            // refreshToken 검증
            Claims claims = (Claims) jwtUtil.validateToken(refreshToken);

            Long uid = claims.get("uid", Long.class);
            String userId = claims.get("userId", String.class);
            String uname = claims.get("uname", String.class);
            String role = claims.get("role", String.class);
            Long shopId = claims.get("shopId", Long.class);

//            log.debug("Refresh claims: uid={}, userId={}, role={}", uid, userId, role);

            // 토큰 정보 조립
            Map<String, Object> tokenClaims = new HashMap<>();
            tokenClaims.put("uid", uid);
            tokenClaims.put("userId", userId);
            tokenClaims.put("uname", uname);
            tokenClaims.put("role", role);
            // shopId가 있는 경우에만 추가
            if (shopId != null) tokenClaims.put("shopId", shopId);

            // 새로운 액세스 토큰 발급
            String newAccessToken = jwtUtil.createToken(tokenClaims, 5);
            CookieUtil.addAccessTokenCookie(response, newAccessToken);

            // 필요한 사용자 정보 반환
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("uid", uid);
            userInfo.put("userId", userId);
            userInfo.put("uname", uname);
            userInfo.put("role", role);
            if (shopId != null) userInfo.put("shopId", shopId);

            return userInfo;
        }  catch (Exception e) {
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
