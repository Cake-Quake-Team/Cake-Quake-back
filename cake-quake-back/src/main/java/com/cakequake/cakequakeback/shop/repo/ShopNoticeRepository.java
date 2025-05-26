package com.cakequake.cakequakeback.shop.repo;

import com.cakequake.cakequakeback.shop.entities.ShopNotice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShopNoticeRepository extends JpaRepository<ShopNotice, Long> {
    Optional<ShopNotice> findTopByShopIdOrderByCreatedDateDesc(Long shopId);
}
