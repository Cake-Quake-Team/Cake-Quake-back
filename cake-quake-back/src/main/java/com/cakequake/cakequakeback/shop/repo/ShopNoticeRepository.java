package com.cakequake.cakequakeback.shop.repo;

import com.cakequake.cakequakeback.shop.dto.ShopNoticeDetailDTO;
import com.cakequake.cakequakeback.shop.entities.ShopNotice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ShopNoticeRepository extends JpaRepository<ShopNotice, Long> {

    //매장 상세 조회에서 공지사항 미리보기
    @Query("""
    SELECT sn
    FROM ShopNotice sn
    WHERE sn.shop.shopId = :shopId
    ORDER BY sn.regDate DESC""")
    List<ShopNotice> findLatestByShopId(@Param("shopId") Long shopId, Pageable pageable);

    // 공지사항 목록 조회 (isVisible = true)
    @Query("SELECT new com.cakequake.cakequakeback.shop.dto.ShopNoticeDetailDTO(sn.shopNoticeId, s.shopId, " +
            "sn.title, sn.content, sn.regDate, sn.modDate) " +
            "FROM ShopNotice sn JOIN sn.shop s " +
            "WHERE s.shopId = :shopId ")
    Page<ShopNoticeDetailDTO> findNoticesByShopId(@Param("shopId") Long shopId, Pageable pageable);

    // 상세 조회
    @Query("SELECT new com.cakequake.cakequakeback.shop.dto.ShopNoticeDetailDTO(sn.shopNoticeId, s.shopId, sn.title, " +
            "sn.content, sn.regDate, sn.modDate) " +
            "FROM ShopNotice sn JOIN sn.shop s " +
            "WHERE sn.shopNoticeId = :noticeId")
    Optional<ShopNoticeDetailDTO> findNoticeDetailById(@Param("noticeId") Long noticeId);

}

