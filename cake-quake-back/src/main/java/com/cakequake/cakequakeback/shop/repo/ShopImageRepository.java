package com.cakequake.cakequakeback.shop.repo;

import com.cakequake.cakequakeback.shop.dto.ShopImageDTO;
import com.cakequake.cakequakeback.shop.entities.Shop;
import com.cakequake.cakequakeback.shop.entities.ShopImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ShopImageRepository extends JpaRepository<ShopImage, Long> {

    //매장 이미지 목록 조회
    @Query("SELECT new com.cakequake.cakequakeback.shop.dto.ShopImageDTO(si.shopImageId,si.shopImageUrl,si.isThumbnail) FROM ShopImage si WHERE si.shop.shopId=:shopId")
    List<ShopImageDTO> findShopImages(@Param("shopId") Long shopId);

    List<ShopImage> findByShop(Shop shop);

    void deleteByShop(Shop shop);

}
