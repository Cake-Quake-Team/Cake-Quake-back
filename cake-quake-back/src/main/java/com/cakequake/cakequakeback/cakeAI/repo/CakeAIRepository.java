package com.cakequake.cakequakeback.cakeAI.repo;

import com.cakequake.cakequakeback.cakeAI.entities.CakeAI;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CakeAIRepository extends JpaRepository<CakeAI, Long> {
    // 특정 세션 ID에 해당하는 모든 대화 기록을 가져오는 메서드
    List<CakeAI> findBySessionIdOrderByRegDateAsc(String sessionId);
}
