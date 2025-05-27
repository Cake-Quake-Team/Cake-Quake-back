package com.cakequake.cakequakeback.cakeItem;

import com.cakequake.cakequakeback.cake.item.CakeCategory;
import com.cakequake.cakequakeback.cake.item.entities.CakeItem;
import com.cakequake.cakequakeback.cake.item.repo.CakeItemRepository;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.shop.entities.Shop;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

@SpringBootTest
@Slf4j
public class CakeItemRepoTests {

    @Autowired
    private CakeItemRepository cakeItemRepository;

    @Test
    @Rollback(false) // 저장 확인하려면 롤백 막기
    void insertDummyCakeItemsWithShopId() {

        Shop savedShop = Shop.builder()
                        .shopId(1L)     // 실제 DB에 존재하는 매장 ID
                        .build();

        Member dummyMemberRef = Member.builder()
                .uid(202L)  // 실제 DB에 존재하는 Member의 ID
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

}
