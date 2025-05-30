package com.cakequake.cakequakeback.cake.option.repo;

import com.cakequake.cakequakeback.cake.option.dto.CakeOptionItemDTO;
import com.cakequake.cakequakeback.cake.option.entities.OptionItem;
import com.cakequake.cakequakeback.cake.option.entities.OptionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OptionItemRepository extends JpaRepository<OptionItem, Long> {

    // 옵션 값 목록 조회
    @Query("SELECT new com.cakequake.cakequakeback.cake.option.dto.CakeOptionItemDTO(oi.optionItemId, oi.optionName, oi.price)" +
            "FROM OptionItem oi WHERE oi.isDeleted = false")
    Page<CakeOptionItemDTO> findOptionItem(@Param("shopId") Long shopId, Pageable pageable);

    // 옵션 타입에 해당하는 옵션 값들 삭제
    @Modifying
    @Query("UPDATE OptionItem oi SET oi.isDeleted = true WHERE oi.optionType = :optionType")
    void markAllDeletedByOptionType(@Param("optionType") OptionType optionType);

}
