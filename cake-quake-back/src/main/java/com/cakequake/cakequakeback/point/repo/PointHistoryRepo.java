package com.cakequake.cakequakeback.point.repo;

import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.point.dto.PointHistoryResponseDTO;
import com.cakequake.cakequakeback.point.entities.PointHistory;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PointHistoryRepo extends JpaRepository<PointHistory, Long> {

    @Query("""
            SELECT new com.cakequake.cakequakeback.point.dto.PointHistoryResponseDTO(
                h.pointHistoryId,
                h.member.uid,
                h.changeType,
                h.description,
                h.amount,
                h.balanceAmount,
                h.regDate
            )
            FROM PointHistory h
            WHERE h.member = :member
            ORDER BY h.regDate DESC
""")
    Page<PointHistoryResponseDTO> findDtoByMemberOrderByRegDateDesc(
            Member member,
            Pageable pageable
    );
}
