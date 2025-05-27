package com.cakequake.cakequakeback.member.repo;

import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.member.entities.MemberRole;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Slf4j
@Transactional
public class MemberRepoTests {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void insertDummyMembers() {
        for (int i = 0; i < 9; i++) {
            Member member = Member.builder()
                    .userId("user" + i)
                    .uname("USER" + i)
                    .password(passwordEncoder.encode("1234"))
                    .role(MemberRole.BUYER)
                    .phoneNumber("010-1234-567" + i)

                    .build();

            memberRepository.save(member);
        } // end for

        List<Member> allMembers = memberRepository.findAll();
        Assertions.assertEquals(9, allMembers.size());

        log.info("총 {}명의 멤버가 저장되었습니다.", allMembers.size());
    }

    @DisplayName("존재하는 아이디가 있는 경우 true 반환")
    @Test
    public void testExistsByUserId() {

        String userId = "testuser";

        Member member = Member.builder()
                .userId(userId)
                .uname("USER")
                .password(passwordEncoder.encode("1234"))
                .role(MemberRole.BUYER)
                .phoneNumber("010-1234-5678")
                .build();

        memberRepository.save(member);

        // when
        boolean exists = memberRepository.existsByUserId(userId);

        // then
        assertTrue(exists);
    }


}
