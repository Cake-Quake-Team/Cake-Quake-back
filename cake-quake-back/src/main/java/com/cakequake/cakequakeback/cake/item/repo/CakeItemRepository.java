package com.cakequake.cakequakeback.cake.item.repo;

import com.cakequake.cakequakeback.cake.item.CakeCategory;
import com.cakequake.cakequakeback.cake.item.dto.CakeDetailDTO;
import com.cakequake.cakequakeback.cake.item.dto.CakeListDTO;
import com.cakequake.cakequakeback.cake.item.entities.CakeItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CakeItemRepository extends JpaRepository<CakeItem, Long> {

    // 전체 상품 조회
    @Query("SELECT new com.cakequake.cakequakeback.cake.item.dto.CakeListDTO(c.cakeId, c.cname, c.price, c.thumbnailImageUrl)" +
            "FROM CakeItem c " +
            "WHERE c.isDeleted = false AND c.category = :keyword")
    Page<CakeListDTO> findAllCakeList(@Param("keyword") CakeCategory category, Pageable pageable);

    // 특정 매장의 상품 목록 조회
    @Query("SELECT new com.cakequake.cakequakeback.cake.item.dto.CakeListDTO(c.cakeId, c.cname, c.price, c.thumbnailImageUrl)" +
            "FROM CakeItem c " +
            "WHERE c.shop.shopId = :shopId AND c.isDeleted = false AND c.category = :category")
    Page<CakeListDTO> findShopCakeList(@Param("shopId") Long shopId, CakeCategory category, Pageable pageable);
}
