package com.cakequake.cakequakeback.cake.option.repo;

import com.cakequake.cakequakeback.cake.option.dto.CakeOptionTypeDTO;
import com.cakequake.cakequakeback.cake.option.entities.OptionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OptionTypeRepository extends JpaRepository<OptionType, Long> {

    // 옵션 타입 목록 조회
    @Query("SELECT new com.cakequake.cakequakeback.cake.option.dto.CakeOptionTypeDTO(ot.optionTypeId, ot.optionType)" +
            "FROM OptionType ot WHERE ot.shop.shopId = :shopId AND ot.isDeleted = false")
    Page<CakeOptionTypeDTO> findOptionType(@Param("shopId") Long shopId, Pageable pageable);

    // 삭제된 동일 이름의 OptionType이 존재하는지 확인
    Optional<OptionType> findByOptionTypeAndIsDeletedTrue(String optionType);
}
