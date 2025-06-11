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
    @Query("SELECT s, si FROM Shop s " +
            "JOIN s.member m " + // 멤버는 항상 존재한다고 가정
            "LEFT JOIN ShopImage si ON si.shop.shopId = s.shopId " + // ShopImage 엔티티를 직접 조인
            "WHERE s.shopId = :shopId")
    List<Object[]> SelectDTO(@Param("shopId") Long shopId);


    //매장 목록
    @Query("SELECT new com.cakequake.cakequakeback.shop.dto.ShopPreviewDTO(s.shopId, s.shopName, s.address, s.rating,s.thumbnailImageUrl) " +
            "FROM Shop s WHERE s.status = :status")
    Page<ShopPreviewDTO> findAll(@Param("status") ShopStatus status, Pageable pageable);

    // 중복 검사용
    boolean existsByBusinessNumber(String businessNumber);

    boolean existsByPhone(String phone);
}
