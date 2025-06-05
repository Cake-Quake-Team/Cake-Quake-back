package com.cakequake.cakequakeback.shop.repo;

import com.cakequake.cakequakeback.cake.item.CakeCategory;
import com.cakequake.cakequakeback.cake.item.entities.CakeImage;
import com.cakequake.cakequakeback.cake.item.entities.CakeItem;
import com.cakequake.cakequakeback.cake.item.repo.CakeImageRepository;
import com.cakequake.cakequakeback.cake.item.repo.CakeItemRepository;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.member.entities.MemberRole;
import com.cakequake.cakequakeback.member.repo.MemberRepository;
import com.cakequake.cakequakeback.shop.dto.ShopDetailResponseDTO;
import com.cakequake.cakequakeback.shop.dto.ShopNoticeDetailDTO;
import com.cakequake.cakequakeback.shop.dto.ShopPreviewDTO;
import com.cakequake.cakequakeback.shop.entities.Shop;
import com.cakequake.cakequakeback.shop.entities.ShopNotice;
import com.cakequake.cakequakeback.shop.entities.ShopStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.cakequake.cakequakeback.member.entities.QMember.member;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
@Transactional
public class ShopRepoTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private ShopNoticeRepository shopNoticeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CakeItemRepository cakeItemRepository;

    @Autowired
    private CakeImageRepository imageRepository;

    //Buyer 데이터 추가
    @Test
    public void insertShopsForBuyers() {

        // Step 1: BUYER 역할을 가진 회원 조회
        List<Member> buyerMembers = memberRepository.findAll().stream()
                .filter(member -> member.getRole() == MemberRole.BUYER)
                .collect(Collectors.toList());

        // Step 2: 각 BUYER에 대해 샵 데이터 생성
        for (int i = 0; i < buyerMembers.size(); i++) {
            Member buyer = buyerMembers.get(i);

            Shop shop = Shop.builder()
                    .member(buyer)
                    .businessNumber("123-45-6789" + i)
                    .shopName("CakeQuake Shop " + i)
                    .address("서울시 강남구 가게로 " + (i + 1))
                    .bossName("김민솔")
                    .phone("02-1234-567" + i)
                    .content("케이크 맛집 " + i)
                    .rating(BigDecimal.valueOf(4.0 + (i % 2) * 0.5))  // 4.0 or 4.5
                    .reviewCount(10 + i)
                    .openTime(LocalTime.of(10, 00))
                    .closeTime(LocalTime.of(20, 00))
                    .closeDays("일요일")
                    .websiteUrl("https://cakequake" + i + ".com")
                    .instagramUrl("https://instagram.com/cakequake" + i)
                    .lat(BigDecimal.valueOf(37.5 + i * 0.001))
                    .lng(BigDecimal.valueOf(127.0 + i * 0.001))
                    .status(ShopStatus.Active) // 상태도 지정해두는 게 좋음
                    .build();

            shopRepository.save(shop);
        }

        // Step 3: 저장된 shop 수 검증
        List<Shop> allShops = shopRepository.findAll();
        assertEquals(buyerMembers.size(), allShops.size());

        log.info("총 {}개의 매장이 BUYER 회원에 의해 생성되었습니다.", allShops.size());
    }

    //공지사항 데이터 추가
    @Test
    public void insertNotice() {
        long startShopId = 1;
        long endShopId = 5;
        int noticeCountPerShop = 2;
        int totalSaved = 0;

        for (long shopId = startShopId; shopId <= endShopId; shopId++) {
            Optional<Shop> optionalShop = shopRepository.findById(shopId);

            if (optionalShop.isEmpty()) {
                System.out.println("❌ Shop with ID " + shopId + " not found.");
                continue;
            }

            Shop shop = optionalShop.get();

            for (int i = 1; i <= noticeCountPerShop; i++) {
                ShopNotice notice = ShopNotice.builder()
                        .shop(shop)
                        .title("공지사항 제목 " + i + " (매장 ID: " + shopId + ")")
                        .content("이것은 매장 [" + shop.getShopName() + "]의 공지사항 내용 " + i + "입니다.")
                        .build();

                shopNoticeRepository.save(notice);
                totalSaved++;
            }

            System.out.println("✅ Shop ID " + shopId + "에 공지사항 2개 저장 완료.");
        }

        System.out.println("🎉 총 " + totalSaved + "개의 공지사항이 저장되었습니다.");
    }

    @Test
    void insertCake() {

        Shop savedShop = Shop.builder()
                .shopId(1L)     // 실제 DB에 존재하는 매장 ID
                .build();

        Member dummyMemberRef = Member.builder()
                .uid(6L)  // 실제 DB에 존재하는 Member의 ID
                .build();

        for (int i = 1; i <= 5; i++) {
            CakeItem cake = CakeItem.builder()
                    .shop(savedShop)  // id만 있는 Shop 참조 사용
                    .cname("바닐라 케이크 " + i)
                    .category(CakeCategory.DAILY)
                    .description("부드러운 바닐라 케이크 " + i + "호")
                    .price(18000 + (i * 1000))
                    .thumbnailImageUrl("https://cake.jpg")
                    .isOnsale(false)
                    .isDeleted(false)
                    .createdBy(dummyMemberRef)
                    .modifiedBy(dummyMemberRef)
                    .build();

            cakeItemRepository.save(cake);
            log.info("상품이 저장되었습니다. {}", cake.getCname());
        }
    }

    @Test
    void insertDummyCakeImage() {

        Member member = Member.builder()
                .uid(6L)      // 실제 DB에 존재하는 회원ID
                .build();

        CakeItem cakeItem = CakeItem.builder()
                .cakeId(2L)    // 실제 DB에 존재하는 상품 ID
                .build();


        CakeImage image1 = CakeImage.builder()
                .imageUrl("https://example.com/image1.jpg")
                .isThumbnail(true)     // 대표 이미지 여부
                .cakeItem(cakeItem)
                .createdBy(member)
                .modifiedBy(member)
                .build();

        CakeImage image2 = CakeImage.builder()
                .imageUrl("https://example.com/image2.jpg")
                .isThumbnail(false)
                .cakeItem(cakeItem)
                .createdBy(member)
                .modifiedBy(member)
                .build();

        imageRepository.save(image1);
        imageRepository.save(image2);

        System.out.println("✅ 케이크 이미지 더미 데이터 저장 완료!");
    }

    //매장 상세 정보 조회
    @Test
    void testSelectDTO() {
        // 테스트할 shopId 설정 (예: 52번이 존재하고 member도 연관되어 있어야 함)
        Long shopId = 1L;

        Optional<ShopDetailResponseDTO> result = shopRepository.selectDTO(shopId);

        assertTrue(result.isPresent(), "ShopDetailResponseDTO should be present");

        ShopDetailResponseDTO dto = result.get();

        // 필드 검증 (필요에 따라 아래를 수정)
        assertEquals(shopId, dto.getShopId());
        assertNotNull(dto.getUid());
        assertNotNull(dto.getShopName());
        assertNotNull(dto.getAddress());

        // 콘솔 확인용
        System.out.println("✅ Shop Detail DTO: " + dto);

        log.info("✅ Shop Detail DTO: shopId={}, uid={}, shopName={}, address={}",
                dto.getShopId(),
                dto.getUid(),
                dto.getShopName(),
                dto.getAddress());
    }

    //매장 목록 조회
    @Test
    void testFindAllShopPreviewDTOByStatus() {

        ShopStatus status = ShopStatus.Active;

        Pageable pageable = PageRequest.of(0, 10); // 1페이지, 페이지당 10개

        // when
        Page<ShopPreviewDTO> shopPage = shopRepository.findAll(status, pageable);

        // then
        assertNotNull(shopPage);
        assertTrue(shopPage.getContent().size() >= 0); // 데이터 없어도 통과하게

        shopPage.getContent().forEach(dto -> {
            System.out.println("🔍 ShopPreviewDTO: " + dto);
            assertNotNull(dto.getShopId());
            assertNotNull(dto.getShopName());
            assertNotNull(dto.getAddress());
        });
    }

    //공지사항 미리보기
    @Test
    void findNoticePreview() {
        // given: 실제 DB에 존재하는 shopId를 사용
        Long existingShopId = 1L; // 실제 존재하는 Shop ID로 교체

        Pageable pageable = PageRequest.of(0, 5); // 최신 5개 조회

        // when
        List<ShopNotice> notices = shopNoticeRepository.findLatestByShopId(existingShopId, pageable);

        // then
        assertThat(notices).isNotNull();
        assertThat(notices.size()).isLessThanOrEqualTo(5);

        // 날짜가 최신순으로 정렬됐는지 간단 검증
        for (int i = 1; i < notices.size(); i++) {
            LocalDateTime prev = notices.get(i - 1).getRegDate();
            LocalDateTime curr = notices.get(i).getRegDate();
            assertThat(prev).isAfterOrEqualTo(curr);
        }

        // 콘솔 출력 (확인용)
        notices.forEach(n -> System.out.println(n.getTitle() + " | " + n.getRegDate()));
    }

    //공지사항 목록 조회
    @Test
    void findNoticesList() {
        Long shopId = 1L;
        Pageable pageable = PageRequest.of(0, 5);

        // when
        Page<ShopNoticeDetailDTO> result = shopNoticeRepository.findNoticesByShopId(shopId, pageable);

        // then
        assertThat(result).isNotNull();

        // 결과 출력
        result.getContent().forEach(dto -> {
            System.out.println("공지 ID: " + dto.getShopNoticeId());
            System.out.println("제목: " + dto.getTitle());
            System.out.println("내용: " + dto.getContent());
            System.out.println("등록일: " + dto.getRegDate());
            System.out.println("---");
        });
    }

    //공지사항 상세 조회
    @Test
    void testNoticeDetail() {
        // given: DB에 존재하는 공지사항 ID 중 하나를 가져옴
        Optional<Long> existingNoticeId = shopNoticeRepository.findAll().stream()
                .map(ShopNotice::getShopNoticeId)
                .findFirst();

        assertThat(existingNoticeId).isPresent();
        Long noticeId = existingNoticeId.get();

        // when
        Optional<ShopNoticeDetailDTO> result = shopNoticeRepository.findNoticeDetailById(noticeId);

        // then
        assertThat(result).isPresent();
        ShopNoticeDetailDTO dto = result.get();

        System.out.println("조회된 공지 ID: " + dto.getShopNoticeId());
        System.out.println("가게 ID: " + dto.getShopId());
        System.out.println("제목: " + dto.getTitle());
        System.out.println("내용: " + dto.getContent());
        System.out.println("등록일: " + dto.getRegDate());
    }






}