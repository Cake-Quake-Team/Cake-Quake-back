package com.cakequake.cakequakeback.cakeItem;

import com.cakequake.cakequakeback.cake.item.entities.CakeImage;
import com.cakequake.cakequakeback.cake.item.entities.CakeItem;
import com.cakequake.cakequakeback.cake.item.repo.CakeImageRepository;
import com.cakequake.cakequakeback.member.entities.Member;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;


@SpringBootTest
@Slf4j
class CakeImageRepositoryTest {

    @Autowired
    private CakeImageRepository cakeImageRepository;

    @Test
    @Rollback(false)
    void insertDummyCakeImage() {

        Member member = Member.builder()
                .uid(202L)      // 실제 DB에 존재하는 회원ID
                .build();

        CakeItem cakeItem = CakeItem.builder()
                .cakeId(52L)    // 실제 DB에 존재하는 상품 ID
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

        cakeImageRepository.save(image1);
        cakeImageRepository.save(image2);

        System.out.println("✅ 케이크 이미지 더미 데이터 저장 완료!");
    }
}

