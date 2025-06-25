package com.cakequake.cakequakeback.member.service;

import com.cakequake.cakequakeback.member.dto.AlarmSettingsDTO;
import com.cakequake.cakequakeback.member.dto.ApiResponseDTO;
import com.cakequake.cakequakeback.member.dto.buyer.BuyerModifyDTO;
import com.cakequake.cakequakeback.member.dto.buyer.BuyerProfileResponseDTO;
import com.cakequake.cakequakeback.member.dto.buyer.BuyerSignupRequestDTO;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.member.entities.MemberRole;
import com.cakequake.cakequakeback.member.entities.MemberStatus;
import com.cakequake.cakequakeback.member.entities.SocialType;
import com.cakequake.cakequakeback.member.repo.MemberRepository;
import com.cakequake.cakequakeback.member.service.auth.MemberService;
import com.cakequake.cakequakeback.member.service.auth.MemberServiceImpl;
import com.cakequake.cakequakeback.member.service.buyer.BuyerService;
import com.cakequake.cakequakeback.security.service.AuthenticatedUserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;


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
    private BuyerService buyerService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private AuthenticatedUserService authenticatedUserService;

    @Disabled
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

    // 가짜 로그인
    @BeforeEach
    void setup() {
        Long existingUid = 25L; // 실제 DB에 있는 uid 사용

        Member member = memberRepository.findById(existingUid)
                .orElseThrow(() -> new RuntimeException("해당 uid 회원 없음"));

        when(authenticatedUserService.getCurrentMember())
                .thenReturn(member);
    }

    @Test
    @DisplayName("구매자 프로필 조회 성공")
    public void testGetBuyerProfileSuccess() {
        // given
        Long uid = 11L; // DB에 존재하는 판매자 uid

        // when
        ApiResponseDTO response = buyerService.getBuyerProfile(uid);
        log.debug(response.getData().toString());

        // then
        assertTrue(response.isSuccess());
        assertEquals("구매자 프로필 조회 성공", response.getMessage());

        Object data = response.getData();
        assertNotNull(data);
        assertTrue(data instanceof BuyerProfileResponseDTO);

        BuyerProfileResponseDTO sellerDTO = (BuyerProfileResponseDTO) data;
        assertEquals(uid, sellerDTO.getUid());
    }

    @Disabled
    @Test
    @DisplayName("구매자 프로필 수정 성공 테스트")
    public void testModifyBuyerProfile() {
        Long uid = 11L;

        BuyerModifyDTO dto = BuyerModifyDTO.builder()
                .uname("이수정")
                .phoneNumber("010-1111-2237")
                .build();

        log.debug(dto.toString());
        ApiResponseDTO response = buyerService.modifyBuyerProfile(uid, dto);

        assertTrue(response.isSuccess());
        assertEquals("구매자 프로필 수정 성공", response.getMessage());
    }

    @Disabled
    @Test
    @DisplayName("구매자 알람 설정 수정 성공 테스트")
    public void testModifyBuyerAlarm() {
        Long uid = 11L;

        // AlarmSettingsDTO를 사용하여 알람 설정 추가
        AlarmSettingsDTO alarmSettings = new AlarmSettingsDTO();
        alarmSettings.setAlarm(true);

        ApiResponseDTO response = buyerService.modifyBuyerAlarm(uid, alarmSettings);

        assertTrue(response.isSuccess());
        assertEquals("구매자 알람 설정 수정 성공", response.getMessage());
    }

    @Disabled
    @Test
    @DisplayName("회원이 탈퇴하면 status가 WITHDRAWN으로 변경")
    void testWithdrawMember() {
        Member current = authenticatedUserService.getCurrentMember();
        Long uid = current.getUid();

        // when
        ApiResponseDTO result = buyerService.withdrawBuyer();

        // then
        Member withdrawn = memberRepository.findById(uid)
                .orElseThrow(() -> new RuntimeException("회원이 존재하지 않음"));

        assertEquals(MemberStatus.WITHDRAWN, withdrawn.getStatus());
        assertTrue(result.isSuccess());
        assertEquals("탈퇴가 완료되었습니다.", result.getMessage());
    }

}
