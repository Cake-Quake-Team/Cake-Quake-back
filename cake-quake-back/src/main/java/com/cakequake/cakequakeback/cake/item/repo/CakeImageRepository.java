package com.cakequake.cakequakeback.cake.item.repo;

import com.cakequake.cakequakeback.cake.item.dto.ImageDTO;
import com.cakequake.cakequakeback.cake.item.entities.CakeImage;
import com.cakequake.cakequakeback.cake.item.entities.CakeItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CakeImageRepository extends JpaRepository<CakeImage, Long> {
    // 특정 케이크의 이미지 목록 조회 (DTO 형태로 반환, 클라이언트 전송용)
    @Query("SELECT new com.cakequake.cakequakeback.cake.item.dto.ImageDTO(ci.imageId, ci.imageUrl, ci.isThumbnail) FROM CakeImage ci WHERE ci.cakeItem.cakeId = :cakeId")
    List<ImageDTO> findCakeImages(@Param("cakeId") Long cakeId);

    // 특정 케이크 모든 CakeImage 엔티티 조회 (수정/삭제 등 트랜잭션 작업용)
    List<CakeImage> findByCakeItem(CakeItem cakeItem);

    // 특정 CakeItem에 연관된 모든 CakeImage 삭제
    void deleteByCakeItem(CakeItem cakeItem);
}
