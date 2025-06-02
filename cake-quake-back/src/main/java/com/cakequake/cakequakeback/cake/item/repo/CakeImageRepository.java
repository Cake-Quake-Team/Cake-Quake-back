package com.cakequake.cakequakeback.cake.item.repo;

import com.cakequake.cakequakeback.cake.item.dto.ImageDTO;
import com.cakequake.cakequakeback.cake.item.entities.CakeImage;
import com.cakequake.cakequakeback.cake.item.entities.CakeItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CakeImageRepository extends JpaRepository<CakeImage, Long> {
    // 특정 케이크의 이미지 목록 조회
    @Query("SELECT new com.cakequake.cakequakeback.cake.item.dto.ImageDTO(ci.imageId, ci.imageUrl, ci.isThumbnail) FROM CakeImage ci WHERE ci.cakeItem.cakeId = :cakeId")
    List<ImageDTO> findCakeImages(@Param("cakeId") Long cakeId);


    List<CakeImage> findByCakeItem(CakeItem cakeItem);
}
