package com.cakequake.cakequakeback.cake.item.repo;

import com.cakequake.cakequakeback.cake.item.entities.CakeOptionMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MappingRepository extends JpaRepository<CakeOptionMapping, Long> {
    // 특정 케이크의 옵션 매핑 목록 조회
    List<CakeOptionMapping> findByCakeId(Long cakeId);
}
