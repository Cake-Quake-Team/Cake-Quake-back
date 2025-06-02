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

import java.util.List;
import java.util.Optional;

public interface OptionItemRepository extends JpaRepository<OptionItem, Long> {

    // 옵션 값 목록 조회
    @Query("SELECT new com.cakequake.cakequakeback.cake.option.dto.CakeOptionItemDTO(oi.optionItemId, oi.optionName, oi.price)" +
            "FROM OptionItem oi WHERE oi.isDeleted = false")
    Page<CakeOptionItemDTO> findOptionItem(@Param("shopId") Long shopId, Pageable pageable);

    // 옵션 타입에 해당하는 옵션 값들 삭제
    @Modifying
    @Query("UPDATE OptionItem oi SET oi.isDeleted = true WHERE oi.optionType = :optionType")
    void markAllDeletedByOptionType(@Param("optionType") OptionType optionType);

    // 매핑 시 옵션 값 목록
    @Query("SELECT oi FROM OptionItem oi WHERE oi.optionItemId IN :optionItemIds AND oi.isDeleted = false")
    List<OptionItem> findAllActiveOptionItem(@Param("optionItemIds") List<Long> optionItemIds);

    // 삭제된 동일 이름의 OptionName이 존재하는지 확인
    Optional<OptionItem> findByOptionNameAndIsDeletedTrue(String optionName);
}
