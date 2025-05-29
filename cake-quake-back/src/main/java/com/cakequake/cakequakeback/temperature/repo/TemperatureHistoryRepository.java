package com.cakequake.cakequakeback.temperature.repo;

import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.temperature.entities.Temperature;
import com.cakequake.cakequakeback.temperature.entities.TemperatureHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TemperatureHistoryRepository extends JpaRepository<TemperatureHistory, Long> {

    //uid 이용한 이력 조회
    @Query("SELECT th FROM TemperatureHistory th WHERE th.temperature.member = :member")
    List<TemperatureHistory> findByMember(@Param("member") Member member);

}
