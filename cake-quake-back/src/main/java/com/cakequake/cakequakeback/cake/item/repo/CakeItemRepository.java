package com.cakequake.cakequakeback.cake.item.repo;

import com.cakequake.cakequakeback.cake.item.entities.CakeCategory;
import com.cakequake.cakequakeback.cake.item.dto.CakeListDTO;
import com.cakequake.cakequakeback.cake.item.entities.CakeItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CakeItemRepository extends JpaRepository<CakeItem, Long> {

    // 전체 상품 목록 조회
    @Query("SELECT new com.cakequake.cakequakeback.cake.item.dto.CakeListDTO(c.shop.shopId, c.cakeId, c.cname, c.price, c.thumbnailImageUrl, c.isOnsale, c.orderCount, c.viewCount) " +
            " FROM CakeItem c " +
            " WHERE c.isDeleted = false " +
            " AND (:category IS NULL OR c.category = :category)")
    Page<CakeListDTO> findAllCakeList(@Param("category") CakeCategory category, Pageable pageable);


    // 특정 매장의 상품 목록 조회
    @Query("SELECT new com.cakequake.cakequakeback.cake.item.dto.CakeListDTO(c.shop.shopId, c.cakeId, c.cname, c.price, c.thumbnailImageUrl, c.isOnsale, c.orderCount, c.viewCount) " +
            "FROM CakeItem c " +
            "WHERE c.shop.shopId = :shopId AND c.isDeleted = false " +
            "AND (:category IS NULL OR c.category = :category)")
    Page<CakeListDTO> findShopCakeList(@Param("shopId") Long shopId, @Param("category") CakeCategory category, Pageable pageable);
}
