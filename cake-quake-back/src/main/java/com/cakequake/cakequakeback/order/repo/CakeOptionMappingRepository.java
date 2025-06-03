package com.cakequake.cakequakeback.order.repo;

import com.cakequake.cakequakeback.cake.item.entities.CakeOptionMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * CakeOptionMapping 엔티티용 레포지토리
 * - 필요 시, 옵션 매핑(CakeOptionMapping) 정보를 직접 조회할 때 사용
 */
@Repository
public interface CakeOptionMappingRepository extends JpaRepository<CakeOptionMapping, Long> {

    Optional<CakeOptionMapping> findById(Long id);
    // 추가 커스텀 메서드가 필요하면 여기에 선언
}
