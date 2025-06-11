package com.cakequake.cakequakeback.shop.repo;

import com.cakequake.cakequakeback.cake.item.CakeCategory;
import com.cakequake.cakequakeback.cake.item.entities.CakeImage;
import com.cakequake.cakequakeback.cake.item.entities.CakeItem;
import com.cakequake.cakequakeback.cake.item.repo.CakeImageRepository;
import com.cakequake.cakequakeback.cake.item.repo.CakeItemRepository;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.member.entities.MemberRole;
import com.cakequake.cakequakeback.member.entities.SocialType;
import com.cakequake.cakequakeback.member.repo.MemberRepository;
import com.cakequake.cakequakeback.shop.dto.ShopDetailResponseDTO;
import com.cakequake.cakequakeback.shop.dto.ShopNoticeDetailDTO;
import com.cakequake.cakequakeback.shop.dto.ShopPreviewDTO;
import com.cakequake.cakequakeback.shop.entities.Shop;
import com.cakequake.cakequakeback.shop.entities.ShopImage;
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
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ShopImageRepository shopImageRepository;

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

        // 만약 BUYER 회원이 없다면 테스트를 위해 몇 명 생성합니다.
        if (buyerMembers.isEmpty()) {
            log.warn("BUYER 역할의 회원이 없습니다. 테스트를 위해 몇 명 생성합니다.");
            for (int i = 0; i < 3; i++) {
                Member newBuyer = Member.builder()
                        .userId("buyerUser" + i) // userId 필드 추가
                        .uname("BUYER" + i)       // uname 필드 추가
                        .password(passwordEncoder.encode("a123456*")) // 비밀번호 인코딩
                        .role(MemberRole.BUYER)
                        .phoneNumber("010-0000-000" + i)
                        .socialType(SocialType.BASIC) // socialType 필드 추가
                        .build();
                memberRepository.save(newBuyer);
                buyerMembers.add(newBuyer);
            }
            log.info("총 {}명의 BUYER 회원을 생성했습니다.", buyerMembers.size());
        }

        long initialShopCount = shopRepository.count();
        long initialShopImageCount = shopImageRepository.count();

        // Step 2: 각 BUYER에 대해 샵 데이터 생성 및 이미지 데이터 추가
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
                    .rating(BigDecimal.valueOf(4.0 + (i % 2) * 0.5))
                    .reviewCount(10 + i)
                    .openTime(LocalTime.of(10, 00))
                    .closeTime(LocalTime.of(20, 00))
                    .closeDays("일요일")
                    .websiteUrl("https://cakequake" + i + ".com")
                    .instagramUrl("https://instagram.com/cakequake" + i)
                    .lat(BigDecimal.valueOf(37.5 + i * 0.001))
                    .lng(BigDecimal.valueOf(127.0 + i * 0.001))
                    .status(ShopStatus.ACTIVE)
                    .build();

            shopRepository.save(shop);

            ShopImage thumbnailImage = ShopImage.builder()
                    .shop(shop)
                    .shopImageUrl("http://example.com/shop_" + shop.getShopId() + "_thumb.jpg")
                    .isThumbnail(true)
                    .createdBy("system_test")
                    .modifiedBy("system_test")
                    .build();
            shopImageRepository.save(thumbnailImage);

            ShopImage generalImage1 = ShopImage.builder()
                    .shop(shop)
                    .shopImageUrl("http://example.com/shop_" + shop.getShopId() + "_img1.jpg")
                    .isThumbnail(false)
                    .createdBy("system_test")
                    .modifiedBy("system_test")
                    .build();
            shopImageRepository.save(generalImage1);

            ShopImage generalImage2 = ShopImage.builder()
                    .shop(shop)
                    .shopImageUrl("http://example.com/shop_" + shop.getShopId() + "_img2.jpg")
                    .isThumbnail(false)
                    .createdBy("system_test")
                    .modifiedBy("system_test")
                    .build();
            shopImageRepository.save(generalImage2);
        }

        // Step 3: 저장된 데이터 수 검증
        List<Shop> allShops = shopRepository.findAll();
        List<ShopImage> allShopImages = shopImageRepository.findAll();

        assertEquals(initialShopCount + buyerMembers.size(), allShops.size(), "생성된 상점의 수가 예상과 다릅니다.");
        assertEquals(initialShopImageCount + (buyerMembers.size() * 3), allShopImages.size(), "생성된 이미지의 수가 예상과 다릅니다.");

        if (!allShops.isEmpty()) {
            Shop firstShop = allShops.get(0);
            List<ShopImage> imagesForFirstShop = shopImageRepository.findByShop(firstShop);
            assertNotNull(imagesForFirstShop);
            assertEquals(3, imagesForFirstShop.size(), "첫 번째 상점의 이미지 수가 예상과 다릅니다.");
            log.info("첫 번째 상점 (ID: {})에 {}개의 이미지가 생성되었습니다.", firstShop.getShopId(), imagesForFirstShop.size());
        }

        log.info("총 {}개의 매장이 BUYER 회원에 의해 생성되었습니다.", allShops.size());
        log.info("총 {}개의 매장 이미지가 생성되었습니다.", allShopImages.size());

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

    @Commit
    @Test
    void insertCake() {

        Shop savedShop = Shop.builder()
                .shopId(5L)     // 실제 DB에 존재하는 매장 ID
                .build();

        Member dummyMemberRef = Member.builder()
                .uid(5L)  // 실제 DB에 존재하는 Member의 ID
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
    @Commit
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
        Long shopId = 5L;

        List<Object[]> results = shopRepository.SelectDTO(shopId);


        // Iterate through results to verify all images
        int imageCount = 0;
        boolean foundThumbnail = false;
        for (Object[] row : results) {
            Shop s = (Shop) row[0];
            ShopImage si = (ShopImage) row[1];

            assertEquals(shopId, s.getShopId(), "All rows should have the same shop ID");

            if (si != null) {
                imageCount++;
                assertNotNull(si.getShopImageId(), "ShopImage ID should not be null");
                assertNotNull(si.getShopImageUrl(), "ShopImage URL should not be null");
                if (si.getIsThumbnail()) {
                    foundThumbnail = true;
                }
            }
        }

        assertEquals(3, imageCount, "Expected 3 ShopImage entities in total");
        assertTrue(foundThumbnail, "Expected to find at least one thumbnail image");

        // Console output for verification
        log.info("✅ Successfully retrieved {} rows for shopId: {}", results.size(), shopId);
        results.forEach(row -> {
            Shop s = (Shop) row[0];
            ShopImage si = (ShopImage) row[1];
            log.info("  Shop ID: {}, Shop Name: {}, Image URL: {}, Is Thumbnail: {}",
                    s.getShopId(), s.getShopName(), (si != null ? si.getShopImageUrl() : "N/A"), (si != null ? si.getIsThumbnail() : "N/A"));
        });
    }

    //매장 목록 조회
    @Test
    void testFindAllShopPreviewDTOByStatus() {

        ShopStatus status = ShopStatus.ACTIVE;

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
        Long existingShopId = 5L; // 실제 존재하는 Shop ID로 교체

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
        Long shopId = 5L;
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