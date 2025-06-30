package com.cakequake.cakequakeback.shop.listener;


import com.cakequake.cakequakeback.review.event.ReviewChangedEvent;
import com.cakequake.cakequakeback.review.repo.common.CommonReviewRepo;
import com.cakequake.cakequakeback.shop.repo.ShopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@RequiredArgsConstructor
@Log4j2
public class ShopReviewStatsListener {
    private final CommonReviewRepo commonReviewRepo;
    private final ShopRepository shopRepo;


    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation =  Propagation.REQUIRES_NEW)
    public void onReviewChanged(ReviewChangedEvent event) {
        Long shopId = event.getShopId();
        log.info("[DEBUG] onReviewChanged 호출 → shopId={}", shopId);

        Long cnt = commonReviewRepo.countByShopShopId(shopId);
        BigDecimal avg = commonReviewRepo.avgRatingByShopShopId(shopId)
                .setScale(1, RoundingMode.HALF_UP);
        log.info("[DEBUG] 집계 결과 → count={}, avg={}", cnt, avg);

        shopRepo.findById(shopId).ifPresent(shop -> {
            // Long → int 변환: intValue() 사용
            shop.updateReviewCount( cnt.intValue());
            shop.updateRating(avg);
            log.info("[DEBUG] Shop 엔티티 업데이트 → reviewCount={}, rating={}", shop.getReviewCount(), shop.getRating());
            shopRepo.save(shop);
            log.info("[DEBUG] Shop saved in NEW TX → reviewCount={}, rating={}",
                    shop.getReviewCount(), shop.getRating());
        });
    }

}
