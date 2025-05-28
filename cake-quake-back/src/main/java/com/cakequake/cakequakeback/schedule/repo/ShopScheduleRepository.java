package com.cakequake.cakequakeback.schedule.repo;

import com.cakequake.cakequakeback.schedule.entities.ShopSchedule;
import com.cakequake.cakequakeback.shop.dto.ShopDetailResponseDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ShopScheduleRepository extends JpaRepository<ShopSchedule, Long> {

    //예약 스케줄 조회
    @Query("SELECT s " +
            "FROM ShopSchedule s " +
            "WHERE s.shop.shopId = :shopId " +
            "AND s.scheduleDateTime = :scheduleDateTime")
    Optional<ShopSchedule> findShopSchedule(Long shopId, LocalDateTime scheduleDateTime);

}
