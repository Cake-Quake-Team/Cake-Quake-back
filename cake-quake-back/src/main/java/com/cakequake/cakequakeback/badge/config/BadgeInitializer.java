package com.cakequake.cakequakeback.badge.config;

import com.cakequake.cakequakeback.badge.entities.Badge;
import com.cakequake.cakequakeback.badge.repo.BadgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BadgeInitializer implements ApplicationRunner {

    private final BadgeRepository badgeRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (badgeRepository.count() > 0) {
            return; // 이미 데이터가 있으면 초기화 안 함
        }

        List<Badge> badges = List.of(
                Badge.builder().name("첫 주문 완료").icon("🎉").description("첫 오더 달성").build(),
                Badge.builder().name("취소·반품 없이 5회 구매").icon("🛡️").description("철벽 쇼퍼").build(),
                Badge.builder().name("첫 후기 작성").icon("✍️").description("리뷰 스타터").build(),
                Badge.builder().name("10개 후기 작성").icon("🌟").description("리뷰 마스터").build(),
                Badge.builder().name("3일 연속 구매").icon("🏃‍♂️").description("쇼핑 러쉬").build(),
                Badge.builder().name("총 20회 구매").icon("❤️").description("단골 VIP").build(),
                Badge.builder().name("5개 카테고리 구매").icon("🎯").description("다재다능 쇼퍼").build(),
                Badge.builder().name("누적 결제 100만 원").icon("💰").description("럭셔리 쇼퍼").build(),
                Badge.builder().name("매달 1회 이상 구매").icon("📆").description("연간 쇼퍼").build(),
                Badge.builder().name("1년 무취소").icon("🤗").description("믿음의 쇼퍼").build()
        );

        badgeRepository.saveAll(badges);
    }
}

