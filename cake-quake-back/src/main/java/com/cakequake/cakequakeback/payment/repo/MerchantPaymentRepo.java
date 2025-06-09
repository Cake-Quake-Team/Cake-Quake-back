package com.cakequake.cakequakeback.payment.repo;

import com.cakequake.cakequakeback.payment.entities.MerchantPaymentKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

//매장별 암호화된 PG키 조회/저장용
public interface MerchantPaymentRepo extends JpaRepository<MerchantPaymentKey,Long> {

    @Query("""
       SELECT k
       FROM MerchantPaymentKey k
       WHERE k.shopId = :shopId
       AND k.provider =:provider
       AND k.isActive =:isActive
    """)
    Optional<MerchantPaymentKey> findByShopIdAndProviderAndIsActive(
      @Param("shopId") Long shopId,
      @Param("provider") String provider,
      @Param("isActive") boolean isActive
    );


}
