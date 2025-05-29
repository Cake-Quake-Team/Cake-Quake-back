package com.cakequake.cakequakeback.member.service;

import com.cakequake.cakequakeback.member.dto.ApiResponseDTO;
import com.cakequake.cakequakeback.member.dto.buyer.BuyerSignupRequestDTO;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.member.entities.MemberRole;
import com.cakequake.cakequakeback.member.entities.SocialType;
import com.cakequake.cakequakeback.member.repo.MemberRepository;
import com.cakequake.cakequakeback.member.validator.MemberValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberValidator memberValidator;

    public ApiResponseDTO signup(BuyerSignupRequestDTO requestDTO) {
        log.debug("---------signup--------------");

        SocialType joinType = SocialType.from(requestDTO.getJoinType());

        /*
        유효성 형식 검사 - userId, 비밀번호, uname 길이, 전화번호 형식, 가입 방식
        중복 검사 - userId
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
                throw new IllegalArgumentException("지원하지 않는 가입 방식입니다.");
        }

        // 휴대폰 인증은 프론트에서 따로 호출

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

}
