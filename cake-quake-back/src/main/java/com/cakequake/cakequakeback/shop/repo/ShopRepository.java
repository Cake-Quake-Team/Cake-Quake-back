package com.cakequake.cakequakeback.shop.repo;

import com.cakequake.cakequakeback.shop.dto.ShopDetailResponseDTO;
import com.cakequake.cakequakeback.shop.dto.ShopPreviewDTO;
import com.cakequake.cakequakeback.shop.entities.Shop;
import com.cakequake.cakequakeback.shop.entities.ShopNotice;
import com.cakequake.cakequakeback.shop.entities.ShopStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ShopRepository extends JpaRepository<Shop, Long> {
    //매장 정보 상세 조회
    @Query("SELECT new com.cakequake.cakequakeback.shop.dto.ShopDetailResponseDTO(" +
            "s.shopId, m.uid, s.businessNumber, s.shopName, s.address, s.phone, s.content, " +
            "s.rating, s.reviewCount, s.operationHours, s.closeDays, s.websiteUrl, " +
            "s.instagramUrl, s.status, s.lat, s.lng) " +
            "FROM Shop s " +
            "JOIN s.member m " +
            "WHERE s.shopId = :shopId")
    Optional<ShopDetailResponseDTO> selectDTO(@Param("shopId") Long shopId);

    //매장 목록
    @Query("SELECT new com.cakequake.cakequakeback.shop.dto.ShopPreviewDTO(s.shopId, s.shopName, s.address, s.rating) " +
            "FROM Shop s WHERE s.status = :status")
    Page<ShopPreviewDTO> findAll(@Param("status") ShopStatus status, Pageable pageable);

}
