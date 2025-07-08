package com.cakequake.cakequakeback.shop.repo;

import com.cakequake.cakequakeback.shop.dto.ShopPreviewDTO;
import com.cakequake.cakequakeback.schedule.dto.ShopScheduleDTO;
import com.cakequake.cakequakeback.shop.entities.Shop;
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
            "JOIN s.member m " +
            "LEFT JOIN ShopImage si ON si.shop.shopId = s.shopId " + // ShopImage 엔티티를 직접 조인
            "WHERE s.shopId = :shopId")
    List<Object[]> SelectDTO(@Param("shopId") Long shopId);


    //매장 목록
    @Query("SELECT new com.cakequake.cakequakeback.shop.dto.ShopPreviewDTO(s.shopId, s.shopName, s.address, s.rating,s.thumbnailImageUrl, s.member.uid) " +
            "FROM Shop s WHERE s.status = :status")
    Page<ShopPreviewDTO> findAll(@Param("status") ShopStatus status, Pageable pageable);

    @Query("SELECT new com.cakequake.cakequakeback.schedule.dto.ShopScheduleDTO(" +
            "s.shopId, s.shopName, s.address, s.rating, s.thumbnailImageUrl, " + // 기존 필드
            "s.openTime, s.closeTime, s.closeDays) " + // <<-- 이 필드들을 추가해야 합니다.
            "FROM Shop s WHERE s.status = :status")
    Page<ShopScheduleDTO> findShopPreviewDTOByStatus(@Param("status") ShopStatus status,Pageable pageable);

    //검색어, 필터, 상태 모두 고려
    @Query("SELECT new com.cakequake.cakequakeback.shop.dto.ShopPreviewDTO(s.shopId, s.shopName, s.address, s.rating, s.thumbnailImageUrl,s.member.uid) " +
            "FROM Shop s " +
            "WHERE (CASE WHEN :filterStatus IS NOT NULL THEN s.status = :filterStatus ELSE s.status = :baseStatus END) " +
            "AND (:keyword IS NULL OR s.shopName LIKE %:keyword% OR s.address LIKE %:keyword%) ")
    Page<ShopPreviewDTO> findShopPreviewsByStatusAndKeywordAndFilter(
            @Param("baseStatus") ShopStatus baseStatus,
            @Param("keyword") String keyword,
            @Param("filterStatus") ShopStatus filterStatus,
            Pageable pageable);

    // 중복 검사용
    boolean existsByBusinessNumber(String businessNumber);

    boolean existsByPhone(String phone);

    // 매장 요약 정보 가져오기
    @Query("SELECT new com.cakequake.cakequakeback.shop.dto.ShopPreviewDTO(" +
            " s.shopId, s.shopName, s.address) FROM Shop s WHERE s.member.uid = :uid")
    Optional<ShopPreviewDTO> findPreviewByUid(@Param("uid") Long uid);

    // 판매자 탈퇴 시 shop status 변경용
    @Query("SELECT s FROM Shop s WHERE s.member.uid = :uid AND s.status = 'ACTIVE'")
    Optional<Shop> findActiveShopByUid(@Param("uid") Long uid);

    Optional<Shop> findByMember_Uid(Long memberId); // 특정 Member가 소유한 Shop 찾기

}
