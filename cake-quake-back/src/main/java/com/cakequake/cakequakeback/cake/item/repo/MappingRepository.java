package com.cakequake.cakequakeback.cake.item.repo;

import com.cakequake.cakequakeback.cake.item.entities.CakeItem;
import com.cakequake.cakequakeback.cake.item.entities.CakeOptionMapping;
import com.cakequake.cakequakeback.cake.option.entities.OptionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MappingRepository extends JpaRepository<CakeOptionMapping, Long> {
    // 특정 케이크의 옵션 매핑 목록 조회
    List<CakeOptionMapping> findByCakeItemCakeId(Long cakeId);

    // 케이크 ID에 연결된 옵션 값 목록 조회
    @Query("SELECT m.optionItem FROM CakeOptionMapping m WHERE m.cakeItem.cakeId = :cakeId AND m.isUsed = true")
    List<OptionItem> findOptionItemsByCakeId(@Param("cakeId") Long cakeId);

    // 특정 상품에 연결된 모든 옵션 매핑 조회
    List<CakeOptionMapping> findByCakeItem(CakeItem cakeItem);

    // 상품이 삭제되면 연결된 옵션 매핑도 삭제
    void deleteByCakeItem(CakeItem cakeItem);
}
