package com.cakequake.cakequakeback.member.repo;

import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.member.entities.MemberRole;
import com.cakequake.cakequakeback.member.entities.SocialType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Slf4j
@Transactional
public class MemberRepoTests {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /*
        2025.06.05 수정
        @Commit 있어야 실제 db에 저장됨.
        socialType(SocialType.BASIC) 추가
        - 역할별로 1명씩 저장하는 테스트 메서드 추가
     */
    //@Commit
    @Test
    public void insertDummyMembers() {
        memberRepository.deleteAll(); // DB 초기화_ 9명 추가 테스트 성공 확인을 위해 초기화.

        for (int i = 0; i < 9; i++) {
            Member member = Member.builder()
                    .userId("user" + i)
                    .uname("USER" + i)
                    .password(passwordEncoder.encode("a123456*"))
                    .role(MemberRole.BUYER)
                    .phoneNumber("010-1234-567" + i)
                    .socialType(SocialType.BASIC)
                    .build();

            memberRepository.save(member);
        } // end for

        List<Member> allMembers = memberRepository.findAll();
        Assertions.assertEquals(9, allMembers.size());

        log.info("총 {}명의 멤버가 저장되었습니다.", allMembers.size());
    }

//    @Commit
    @Test
    public void insertDummyBuyer() {
        Member seller = Member.builder()
                .userId("buyer1")
                .uname("BUYER1")
                .password(passwordEncoder.encode("a123456*"))
                .role(MemberRole.BUYER)
                .phoneNumber("010-1234-0001")
                .socialType(SocialType.BASIC)
                .build();

        memberRepository.save(seller);

        Optional<Member> result = memberRepository.findByUserId("buyer1");
        Assertions.assertTrue(result.isPresent());
        log.info("유저 저장 완료: {}", result.get());
    }

//    @Commit
    @Test
    public void insertDummySeller() {
        Member seller = Member.builder()
                .userId("seller1")
                .uname("SELLER1")
                .password(passwordEncoder.encode("a123456*"))
                .role(MemberRole.SELLER)
                .phoneNumber("010-5678-0001")
                .socialType(SocialType.BASIC)
                .build();

        memberRepository.save(seller);

        Optional<Member> result = memberRepository.findByUserId("seller1");
        Assertions.assertTrue(result.isPresent());
        log.info("판매자 저장 완료: {}", result.get());
    }

//    @Commit
    @Test
    public void insertDummyAdmin() {
        Member admin = Member.builder()
                .userId("admin1")
                .uname("ADMIN1")
                .password(passwordEncoder.encode("a123456*"))
                .role(MemberRole.ADMIN)
                .phoneNumber("010-9999-0001")
                .socialType(SocialType.BASIC)
                .build();

        memberRepository.save(admin);

        Optional<Member> result = memberRepository.findByUserId("admin1");
        Assertions.assertTrue(result.isPresent());
        log.info("관리자 저장 완료: {}", result.get());
    }

    @DisplayName("존재하는 아이디가 있는 경우 true 반환")
    @Test
    public void testExistsByUserId() {

        String userId = "user1"; // 실제 db에 있는 id로 테스트 필요.

        // when
        boolean exists = memberRepository.existsByUserId(userId);

        // then
        assertTrue(exists);
    }


}
