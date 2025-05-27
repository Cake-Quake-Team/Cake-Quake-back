package com.cakequake.cakequakeback.shop.repo;

import com.cakequake.cakequakeback.shop.entities.ShopNotice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ShopNoticeRepository extends JpaRepository<ShopNotice, Long> {

    @Query("""
    SELECT sn
    FROM ShopNotice sn
    WHERE sn.shop.shopId = :shopId
    ORDER BY sn.regDate DESC
""")
    List<ShopNotice> findLatestByShopId(@Param("shopId") Long shopId, Pageable pageable);
}
