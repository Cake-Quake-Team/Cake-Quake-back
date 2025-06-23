package com.cakequake.cakequakeback.member.repo;

import com.cakequake.cakequakeback.member.dto.buyer.BuyerProfileResponseDTO;
import com.cakequake.cakequakeback.member.dto.seller.SellerResponseDTO;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.member.entities.MemberRole;
import com.cakequake.cakequakeback.member.entities.MemberStatus;
import com.cakequake.cakequakeback.member.entities.SocialType;
import com.cakequake.cakequakeback.shop.dto.ShopPreviewDTO;
import com.cakequake.cakequakeback.shop.repo.ShopRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
@Transactional
public class MemberRepoTests {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ShopRepository shopRepository;

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
                    .status(MemberStatus.ACTIVE) // 명시적 설정
                    .build();

            memberRepository.save(member);
        } // end for

        List<Member> allMembers = memberRepository.findAll();
        assertEquals(9, allMembers.size());

        log.info("총 {}명의 멤버가 저장되었습니다.", allMembers.size());
    }

    //@Commit
    @Test
    public void insertDummyBuyer() {
        Member seller = Member.builder()
                .userId("buyer1")
                .uname("BUYER1")
                .password(passwordEncoder.encode("a123456*"))
                .role(MemberRole.BUYER)
                .phoneNumber("010-1234-0001")
                .socialType(SocialType.BASIC)
                .status(MemberStatus.ACTIVE) // 명시적 설정
                .build();

        memberRepository.save(seller);

//        Optional<Member> result = memberRepository.findByUserId("buyer1");
        Optional<Member> result = memberRepository.findByUserIdAndStatus("buyer1", MemberStatus.ACTIVE);
        Assertions.assertTrue(result.isPresent());
        log.info("유저 저장 완료: {}", result.get());
    }

    //@Commit
    @Test
    public void insertDummySeller() {
        Member seller = Member.builder()
                .userId("seller1")
                .uname("SELLER1")
                .password(passwordEncoder.encode("a123456*"))
                .role(MemberRole.SELLER)
                .phoneNumber("010-5678-0001")
                .socialType(SocialType.BASIC)
                .status(MemberStatus.ACTIVE) // 명시적 설정
                .build();

        memberRepository.save(seller);

//        Optional<Member> result = memberRepository.findByUserId("seller1");
        Optional<Member> result = memberRepository.findByUserIdAndStatus("seller1", MemberStatus.ACTIVE);
        Assertions.assertTrue(result.isPresent());
        log.info("판매자 저장 완료: {}", result.get());
    }

    //@Commit
    @Test
    public void insertDummyAdmin() {
        Member admin = Member.builder()
                .userId("admin2")
                .uname("ADMIN2")
                .password(passwordEncoder.encode("a123456*"))
                .role(MemberRole.ADMIN)
                .phoneNumber("010-9999-0002")
                .socialType(SocialType.BASIC)
                .status(MemberStatus.ACTIVE) // 명시적 설정
                .build();

        memberRepository.save(admin);

//        Optional<Member> result = memberRepository.findByUserId("admin1");
        Optional<Member> result = memberRepository.findByUserIdAndStatus("admin2", MemberStatus.ACTIVE);
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

    @DisplayName("uid로 판매자 정보를 조회하면 SellerResponseDTO를 반환")
    @Test
    public void testSellerGetOne() {
        // given
        Long testUid = 39L; // 실제 DB에 존재하는 uid 사용 필요
        MemberRole seller = MemberRole.SELLER;

        // when
        Optional<SellerResponseDTO> result = memberRepository.sellerGetOne(testUid);

        // then
        assertTrue(result.isPresent());

        SellerResponseDTO dto = result.get();
        log.info("판매자 정보: {}", dto);

        assertEquals(testUid, dto.getUid());
        assertEquals(seller, dto.getRole());
        assertNotNull(dto.getUserId());
        assertNotNull(dto.getUname());
        assertNotNull(dto.getPhoneNumber());
    }

    // 판매자 조회 페이지에서 보여줄 매장 요약 정보 조회 테스트
    @DisplayName("uid로 매장 요약 정보 조회")
    @Test
    public void testFindPreviewByUid() {
        // given
        Long uid = 39L; // 실제 테스트 DB에 존재하는 uid여야 합니다

        // when
        Optional<ShopPreviewDTO> optionalPreview = shopRepository.findPreviewByUid(uid);

        // then
        assertTrue(optionalPreview.isPresent(), "매장 정보가 존재해야 합니다");

        ShopPreviewDTO dto = optionalPreview.get();
        log.info("shopId={}, shopName={}, address={}",
                dto.getShopId(), dto.getShopName(), dto.getAddress());

        // 필요 시 추가 검증
        assertNotNull(dto.getShopId());
        assertNotNull(dto.getShopName());
        assertNotNull(dto.getAddress());
    }

    @DisplayName("uid로 구매자 정보 조회")
    @Test
    public void testBuyerGetOne() {
        // given
        Long testUid = 11L; // 실제 DB에 존재하는 uid 사용 필요
        MemberRole buyer = MemberRole.BUYER;

        // when
        Optional<BuyerProfileResponseDTO> result = memberRepository.buyerGetOne(testUid);

        // then
        assertTrue(result.isPresent());

        BuyerProfileResponseDTO dto = result.get();
        log.info("구매자 정보: {}", dto);

        assertEquals(testUid, dto.getUid());
        assertNotNull(dto.getUserId());
        assertNotNull(dto.getUname());
        assertNotNull(dto.getPhoneNumber());
        assertNotNull(dto.getAlarm());
    }

    @Test
    @DisplayName("정상 회원은 조회됨")
    public void testFindActiveMember() {
        Optional<Member> result = memberRepository.findByUserIdAndStatus("buyer1", MemberStatus.ACTIVE);
        log.debug(result.toString());
        assertTrue(result.isPresent(), "buyer1은 ACTIVE 상태이므로 조회되어야 함");
    }

    @Test
    @DisplayName("탈퇴한 회원은 조회되지 않음")
    public void testFindWithdrawnMember() {
        Optional<Member> result = memberRepository.findByUserIdAndStatus("testuser13", MemberStatus.ACTIVE);
        log.debug(result.toString());
        assertFalse(result.isPresent(), "testuser13은 WITHDRAWN 상태이므로 조회되면 안 됨");
    }

}
