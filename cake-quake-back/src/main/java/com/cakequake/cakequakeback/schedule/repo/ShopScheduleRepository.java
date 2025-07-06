package com.cakequake.cakequakeback.schedule.repo;

import com.cakequake.cakequakeback.schedule.entities.ReservationStatus;
import com.cakequake.cakequakeback.schedule.entities.ShopSchedule;
import com.cakequake.cakequakeback.shop.entities.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ShopScheduleRepository extends JpaRepository<ShopSchedule, Long> {

    //예약 스케줄 조회
    @Query("SELECT s " +
            "FROM ShopSchedule s " +
            "WHERE s.shop.shopId = :shopId " +
            "AND s.scheduleDateTime = :scheduleDateTime")
    Optional<ShopSchedule> findByShopShopIdAndScheduleDateTime(Long shopId, LocalDateTime scheduleDateTime);

    //툭정 날짜에 스케줄이 하나라도 존재하는 모든 매장
    @Query("SELECT DISTINCT ss.shop FROM ShopSchedule ss " +
            "WHERE ss.scheduleDateTime BETWEEN :startOfDay AND :endOfDay " +
            "AND ss.availableSlots > 0 " + // 슬롯이 남아 있어야 함
            "AND ss.status = :status") // 상태가 AVAILABLE 이어야 함
    List<Shop> findDistinctAvailableShopsByScheduleDateTimeBetweenAndStatus(
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay,
            @Param("status") ReservationStatus status // ReservationStatus 파라미터 추가
    );

    //특정 매장 ID와 날짜 범위에 해당하는 모든 스케줄 조회
    List<ShopSchedule> findByShopShopIdAndScheduleDateTimeBetween(
            Long shopShopId, LocalDateTime startOfDay, LocalDateTime endOfDay
    );

    Optional<ShopSchedule> findByShop_ShopIdAndScheduleDateTime(Long shopId, LocalDateTime scheduleDateTime);


}
