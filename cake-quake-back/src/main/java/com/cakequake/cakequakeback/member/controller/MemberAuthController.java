package com.cakequake.cakequakeback.member.controller;

import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.common.utils.CookieUtil;
import com.cakequake.cakequakeback.common.utils.JWTUtil;
import com.cakequake.cakequakeback.member.dto.*;
import com.cakequake.cakequakeback.member.dto.auth.*;
import com.cakequake.cakequakeback.member.dto.auth2.KakaoSignupInitDTO;
import com.cakequake.cakequakeback.member.dto.auth2.SocialSigninResponseDTO;
import com.cakequake.cakequakeback.member.dto.auth2.SocialSignupRequestDTO;
import com.cakequake.cakequakeback.member.dto.buyer.BuyerSignupRequestDTO;
import com.cakequake.cakequakeback.member.dto.seller.SellerSignupStep1RequestDTO;
import com.cakequake.cakequakeback.member.dto.seller.SellerSignupStep2RequestDTO;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.member.service.auth.MemberService;
import com.cakequake.cakequakeback.member.service.auth2.KakaoLoginService;
import com.cakequake.cakequakeback.member.service.seller.SellerService;
import com.cakequake.cakequakeback.security.service.AuthenticatedUserService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
public class MemberAuthController {

    private final MemberService memberService;
    private final SellerService sellerService;
    private final AuthenticatedUserService authenticatedUserService;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;

    private final KakaoLoginService kakaoLoginService;

    public MemberAuthController(
            MemberService memberService,
            SellerService sellerService,
            AuthenticatedUserService authenticatedUserService,
            PasswordEncoder passwordEncoder, JWTUtil jwtUtil,
            KakaoLoginService kakaoLoginService
    ) {
        this.memberService = memberService;
        this.sellerService = sellerService;
        this.authenticatedUserService = authenticatedUserService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.kakaoLoginService = kakaoLoginService;
    }

    @PostMapping("/signup/buyers")
    public ResponseEntity<ApiResponseDTO> signupBuyer(@RequestBody BuyerSignupRequestDTO dto) {

        ApiResponseDTO response = memberService.signup(dto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup/sellers/step1")
    public ResponseEntity<ApiResponseDTO> signupSellerStep1(@ModelAttribute SellerSignupStep1RequestDTO dto) {

        ApiResponseDTO response = sellerService.registerStepOne(dto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup/sellers/step2")
    public ResponseEntity<ApiResponseDTO> signupSellerStep2(@ModelAttribute SellerSignupStep2RequestDTO dto) {

        ApiResponseDTO response = sellerService.registerStepTwo(dto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup/social")
    public ResponseEntity<ApiResponseDTO> signupSocial(@RequestBody SocialSignupRequestDTO dto,
                                                          HttpServletResponse response) {

        SigninResponseDTO result = memberService.signupSocial(dto);

        // 토큰을 HttpOnly 쿠키로 설정
        CookieUtil.addAuthCookies(response, result.getAccessToken(), result.getRefreshToken());
//        log.debug("+++++MemberAuthController+++signupSocial+++result.getAccessToken(): {}, result.getRefreshToken(): {}", result.getAccessToken().substring(0, 10), result.getRefreshToken().substring(0, 10));

        return ResponseEntity.ok(ApiResponseDTO.builder()
                .success(true)
                .message("소셜 유저의 회원 가입 성공")
                .build());
    }

//    @PostMapping("/signin")
//    public ResponseEntity<SigninResponseDTO> signin(@RequestBody @Valid SigninRequestDTO dto) {
//
//        return ResponseEntity.ok(memberService.signin(dto));
//    }
    @PostMapping("/signin")
    public ResponseEntity<Map<String, Object>> signin(@RequestBody SigninRequestDTO dto,
                                                      HttpServletResponse response) {

        SigninResponseDTO result = memberService.signin(dto);

        // 토큰을 HttpOnly 쿠키로 설정
        CookieUtil.addAuthCookies(response, result.getAccessToken(), result.getRefreshToken());
//        log.debug("+++++MemberAuthController+++signin+++result.getAccessToken(): {}, result.getRefreshToken(): {}", result.getAccessToken().substring(0, 10), result.getRefreshToken().substring(0, 10));

        // 응답 바디에는 토큰을 제외한 로그인 정보만 포함
        Map<String, Object> body = Map.of(
                "userId", result.getUserId(),
                "uname", result.getUname(),
                "role", result.getRole()
        );

        return ResponseEntity.ok(body);
    }

    // 토큰에 필요한 유저 정보 담기. 프론트에서는 이 메서드를 호출해서 유저 정보를 획득.
    @GetMapping("/members/me")
    public ResponseEntity<?> getMyInfo(HttpServletRequest request) {
        String token = CookieUtil.getCookieValue(request, "accessToken"); // 직접 추출
        Claims claims = (Claims) jwtUtil.validateToken(token);

        // 필요한 정보 꺼내기
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("uid", claims.get("uid"));
        userInfo.put("userId", claims.get("userId"));
        userInfo.put("uname", claims.get("uname"));
        userInfo.put("role", claims.get("role"));
        userInfo.put("shopId", claims.get("shopId"));

        return ResponseEntity.ok(userInfo);
    }

//    @PostMapping("/refresh")
//    public ResponseEntity<RefreshTokenResponseDTO> refresh(@RequestHeader("Authorization") String accessTokenStr,
//                                                           @RequestBody RefreshTokenRequestDTO dto) {
//        log.debug("---MemberAuthController---refresh()");
//
//        String accessToken = accessTokenStr.substring(7);
//
//        return ResponseEntity.ok(memberService.refreshTokens(accessToken, dto));
//    }
    @PostMapping("refresh")
    public ResponseEntity<Map<String, Object>> refresh(HttpServletRequest request, HttpServletResponse response) {

        // 리프레시 요청 처리 (토큰 발급 및 쿠키 설정)
        Map<String, Object> userInfo = memberService.refreshTokens(request, response);

        return ResponseEntity.ok(userInfo); // 프론트에서 setUser용
    }

    // 카카오 로그인
    @PostMapping("signin/kakao")
    public ResponseEntity<ApiResponseDTO> getKakao(@RequestHeader("Authorization") String authorization,
                                                   HttpServletResponse response) {

        String accessToken = authorization.replace("Bearer ", "");
        log.debug("---MemberAuthController---getKakao--- accessToken: {}", accessToken.substring(0, 7));

        ApiResponseDTO dto = kakaoLoginService.processKakaoLogin(accessToken);

        Object data = dto.getData();

        // 응답으로 보낼 데이터 객체 초기화
        Map<String, Object> responseData = new HashMap<>();

        // 기존 유저라면(exists(true) = 가입 한 상태) 쿠키 저장
        if (data instanceof SocialSigninResponseDTO signinDTO && signinDTO.isExists()) {
            // 기존 유저인 경우 → 쿠키 저장
            String jwtAccessToken = signinDTO.getAccessToken();
            String jwtRefreshToken = signinDTO.getRefreshToken();
            log.debug("---MemberAuthController---기존 유저 토큰 저장 확인--- jwtAccessToken: {}", jwtAccessToken);
            CookieUtil.addAuthCookies(response, jwtAccessToken, jwtRefreshToken);

            // 응답용 데이터 구성 (필요한 값만 선택)
            responseData.put("exists", true);
            responseData.put("email", signinDTO.getUserId());
            responseData.put("nickname", signinDTO.getUname());
        } else if (data instanceof KakaoSignupInitDTO signupDTO) {
            // 신규 회원인 경우 → 이메일, 닉네임만 전달
            responseData.put("exists", false);
            responseData.put("email", signupDTO.getEmail());
            responseData.put("nickname", signupDTO.getNickname());
        }

        return ResponseEntity.ok(
                    ApiResponseDTO.builder()
                        .success(true)
                        .message("카카오 유저 가입 유무 확인")
                        .data(responseData)
                        .build());
    }

    @PostMapping("/signout")
    public ResponseEntity<Void> signout(HttpServletResponse response) {
        log.debug("---MemberAuthController---signout()");

        CookieUtil.clearAuthCookies(response);
        return ResponseEntity.ok().build();
    }

    // 탈퇴 전 비밀번호 확인
    @PostMapping("/password/verify")
    public ApiResponseDTO verifyPassword(@RequestBody PasswordCheckDTO dto) {
        Member member = authenticatedUserService.getCurrentMember();

        if (!passwordEncoder.matches(dto.getPassword(), member.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        return ApiResponseDTO.builder()
                .success(true)
                .message("비밀번호 확인 완료")
                .build();
    }

    @PatchMapping("/password")
    public ResponseEntity<ApiResponseDTO> changePassword(@RequestBody PasswordChangeDTO dto) {

        ApiResponseDTO response = memberService.changePassword(dto);
        return ResponseEntity.ok(response);
    }

    /*
        테스트 용
     */
//    @PreAuthorize("hasRole('BUYER')")
//    @GetMapping("/token-test")
//    public ResponseEntity<ApiResponseDTO> tokenTest(){
//
//        return ResponseEntity.ok(ApiResponseDTO.builder()
//                .success(true)
//                .message("로그인 후 토큰으로 테스트 접근 성공")
//                .build());
//    }
//
//    @PreAuthorize("hasRole('SELLER')")
//    @GetMapping("/seller-only")
//    public ResponseEntity<ApiResponseDTO> sellerOnly(@AuthenticationPrincipal CustomUserDetails userDetails){
//
//        log.debug("--------sellerOnly()-----------");
//        String role = userDetails.getAuthorities().toString();
//
//        log.debug("Role: {}", role);
//
//        return ResponseEntity.ok(ApiResponseDTO.builder()
//                .success(true)
//                .message("판매자만 접근 성공")
//                .build());
//    }
//
//    @PreAuthorize("hasRole('ADMIN')")
//    @GetMapping("/admin-only")
//    public ResponseEntity<ApiResponseDTO> adminOnly(){
//        return ResponseEntity.ok(ApiResponseDTO.builder()
//                .success(true)
//                .message("관리자만 접근 성공")
//                .build());
//    }



}
