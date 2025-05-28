package com.cakequake.cakequakeback.member.service;

import com.cakequake.cakequakeback.member.dto.buyer.BuyerSignupRequestDTO;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.member.entities.MemberRole;
import com.cakequake.cakequakeback.member.entities.SocialType;
import com.cakequake.cakequakeback.member.repo.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@SpringBootTest
@Import({MemberService.class, BCryptPasswordEncoder.class})
@Slf4j
public class MemberServiceTests {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Disabled
    @Test
    @DisplayName("일반 회원가입 성공 테스트")
    void testBasicSignup() {

        BuyerSignupRequestDTO dto = BuyerSignupRequestDTO.builder()
                .userId("testuser6")
                .uname("테스터6")
                .password("1111")
                .phoneNumber("010-1111-2228")
                .publicInfo(true)
                .alarm(true)
                .role("buyer") // 소문자 대문자 다 가능
                .joinType("basic")
                .build();

        log.debug(dto.toString());

        memberService.signup(dto);

        // 조회해서 비교
        Optional<Member> saved = memberRepository.findByUserId("testuser6");
        assertThat(saved).isPresent();

        Member member = saved.get();

        assertThat(member.getUserId()).isEqualTo("testuser6");
        assertThat(passwordEncoder.matches("1111", member.getPassword())).isTrue(); // 비밀번호 암호화 검증
        assertThat(member.getRole()).isEqualTo(MemberRole.BUYER);
        assertThat(member.getSocialType()).isEqualTo(SocialType.BASIC);
    }
}
