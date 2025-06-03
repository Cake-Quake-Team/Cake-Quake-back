package com.cakequake.cakequakeback.member.service;

import com.cakequake.cakequakeback.member.dto.ApiResponseDTO;
import com.cakequake.cakequakeback.member.dto.buyer.BuyerSignupRequestDTO;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.member.entities.MemberRole;
import com.cakequake.cakequakeback.member.entities.SocialType;
import com.cakequake.cakequakeback.member.repo.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@SpringBootTest
@Import({MemberServiceImpl.class, BCryptPasswordEncoder.class})
@Slf4j
@TestPropertySource(properties = {
        "logging.level.com.cakequake.cakequakeback.member=DEBUG",
        "logging.level.root=INFO"
})
public class MemberServiceTests {

    @Autowired
    private MemberService service;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

//    @Disabled
    @Test
    @DisplayName("일반 회원가입 성공 테스트")
    public void testBasicSignup() {
        BuyerSignupRequestDTO dto = BuyerSignupRequestDTO.builder()
                .userId("testuser12")
                .uname("테스터12")
                .password("a123456*")
                .phoneNumber("010-1111-2234")
                .publicInfo(true)
                .alarm(true)
                .joinType("basic")
                .build();
        // role은 서비스에서 지정

        log.debug(dto.toString());

        ApiResponseDTO response = service.signup(dto);
        // 응답 검증
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("회원 가입에 성공하였습니다.");

        // 조회해서 비교
        Optional<Member> saved = memberRepository.findByUserId("testuser12");
        assertThat(saved).isPresent();

        Member member = saved.get();
        assertThat(member.getUserId()).isEqualTo("testuser12");
        assertThat(passwordEncoder.matches("a123456*", member.getPassword())).isTrue(); // 비밀번호 암호화 검증
        assertThat(member.getRole()).isEqualTo(MemberRole.BUYER);
        assertThat(member.getSocialType()).isEqualTo(SocialType.BASIC);
    }

}
