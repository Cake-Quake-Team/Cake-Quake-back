package com.cakequake.cakequakeback.temperature.repo;

import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.temperature.dto.TemperatureHistoryResponseDTO;
import com.cakequake.cakequakeback.temperature.entities.Temperature;
import com.cakequake.cakequakeback.temperature.entities.TemperatureHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TemperatureHistoryRepository extends JpaRepository<TemperatureHistory, Long> {

    //uid 이용한 이력 조회
    @Query("""
            SELECT new com.cakequake.cakequakeback.temperature.dto.TemperatureHistoryResponseDTO(
                th.historyId,
                th.member.uid,
                th.changeAmount,
                th.afterTemperature,
                th.reason,
                th.regDate
            )
            FROM TemperatureHistory th
            WHERE th.member = :member
            ORDER BY th.regDate DESC
""")
    Page<TemperatureHistoryResponseDTO> findByMember(
            Member member,
            Pageable pageable
    );

}
