package com.cakequake.cakequakeback.schedule.service;

import com.cakequake.cakequakeback.member.repo.MemberRepository;
import com.cakequake.cakequakeback.order.entities.CakeOrder;
import com.cakequake.cakequakeback.order.entities.OrderStatus;
import com.cakequake.cakequakeback.order.repo.SellerOrderRepository;
import com.cakequake.cakequakeback.schedule.entities.ReservationStatus;
import com.cakequake.cakequakeback.schedule.entities.ShopSchedule;
import com.cakequake.cakequakeback.schedule.repo.ShopScheduleRepository;
import com.cakequake.cakequakeback.shop.entities.Shop;
import com.cakequake.cakequakeback.shop.repo.ShopRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ShopScheduleServiceImpl implements ShopScheduleService {
    private final ShopRepository shopRepository;
    private final SellerOrderRepository sellerOrderRepository;
    private final ShopScheduleRepository shopScheduleRepository;


    // ⭐️ 픽업 시간대별 최대 슬롯 수를 고정값으로 정의 (방법 1)
    private static final int DEFAULT_MAX_SLOTS_PER_TIME = 10;

    // 제외할 주문 상태 목록 (이전과 동일)
    private static final List<OrderStatus> EXCLUDED_STATUSES = Arrays.asList(
            OrderStatus.RESERVATION_CANCELLED,
            OrderStatus.NO_SHOW
    );

    // 가능한 모든 픽업 시간 -> 매장 운영 시간 기반 (변경 없음)
    @Override
    public List<LocalTime> getPossiblePickupTime(Long shopId) {
        log.info("⏰ getPossiblePickupTime 호출. (shopId: {})", shopId);
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> {
                    log.error("⏰ Shop ID {}에 해당하는 매장을 찾을 수 없습니다.", shopId);
                    return new IllegalArgumentException("Shop not found with ID: " + shopId);
                });

        LocalTime startTime = shop.getOpenTime();
        LocalTime endTime = shop.getCloseTime();
        int intervalMinutes = 30;

        log.info("⏰ 매장 {} ({})의 픽업 설정: 시작={}, 종료={}, 간격={}분",
                shop.getShopName(), shopId, startTime, endTime, intervalMinutes);
        List<LocalTime> possibleTimes = new ArrayList<>();
        LocalTime currentTime = startTime;

        while (currentTime.isBefore(endTime) || currentTime.equals(endTime)) {
            possibleTimes.add(currentTime);
            currentTime = currentTime.plusMinutes(intervalMinutes);
        }
        log.info("⏰ getPossiblePickupTime 완료. 생성된 가능 시간 수: {}", possibleTimes.size());
        log.debug("⏰ 생성된 모든 가능 시간: {}", possibleTimes);
        return possibleTimes;
    }

    @Override
    public List<LocalTime> getAvailablePickupTimes(Long shopId, LocalDate date) {
        log.info("📅 getAvailablePickupTimes 호출 (Shop 엔티티 필드 없이). (shopId: {}, date: {})", shopId, date);

        // ⭐️ 여기서 DEFAULT_MAX_SLOTS_PER_TIME 또는 this.defaultMaxSlotsPerTime (주입받은 값)을 사용합니다.
        int maxSlotsPerTime = DEFAULT_MAX_SLOTS_PER_TIME; // 또는 this.defaultMaxSlotsPerTime
        log.debug("📅 모든 매장의 픽업 시간대별 기본 최대 슬롯: {}개", maxSlotsPerTime);

        // 해당 매장의 가능한 모든 픽업 시간을 가져옵니다.
        List<LocalTime> possiblePickupTimes = getPossiblePickupTime(shopId);
        List<LocalTime> availableTimes = new ArrayList<>();

        for (LocalTime time : possiblePickupTimes) {
            long activeOrdersCount = sellerOrderRepository.countActiveOrdersForPickupTime(
                    shopId, date, time, EXCLUDED_STATUSES);

            log.debug("📅 매장ID: {}, 날짜: {}, 시간: {}에 현재 유효한 주문 수: {}", shopId, date, time, activeOrdersCount);

            long remainingSlots = maxSlotsPerTime - activeOrdersCount;

            if (remainingSlots > 0) {
                availableTimes.add(time);
            } else {
                log.debug("📅 매장ID: {}, 날짜: {}, 시간: {}은 슬롯 부족 (남은 슬롯: {})", shopId, date, time, remainingSlots);
            }
        }
        availableTimes.sort(Comparator.naturalOrder());
        log.info("✅ getAvailablePickupTimes 완료 (Shop 엔티티 필드 없이). 최종 사용 가능한 시간 수: {}", availableTimes.size());
        log.debug("✅ 최종 사용 가능한 시간 목록: {}", availableTimes);
        return availableTimes;
    }

    // getAvailableShopsByDate 메서드는 이전과 동일하게 동작합니다.
    // Shop 엔티티에서 maxSlotsPerTime을 사용하지 않았으므로, 이 메서드에는 영향을 주지 않습니다.
    @Override
    public List<Shop> getAvailableShopsByDate(LocalDate date) {
        log.info("🏢 getAvailableShopsByDate 호출 (CakeOrder 기반). (date: {})", date);
        List<Shop> shopsWithActiveOrders = sellerOrderRepository.findDistinctShopsWithActiveOrdersOnDate(date, EXCLUDED_STATUSES);
        log.info("✅ getAvailableShopsByDate 완료 (CakeOrder 기반). 최종 사용 가능한 매장 수: {}", shopsWithActiveOrders.size());
        log.debug("✅ 최종 사용 가능한 매장 목록: {}", shopsWithActiveOrders);
        return shopsWithActiveOrders;
    }

    @Override
    public ShopSchedule decreaseSlotsForOrderCreation(CakeOrder order) {
        log.info("[ShopScheduleService] ➡️ decreaseSlotsForOrderCreation 호출 (Shop 엔티티 필드 없이). 주문ID: {}, 픽업일시: {}",
                order.getOrderId(), order.getPickupDate().atTime(order.getPickupTime()));

        Long shopId = order.getShop().getShopId();
        LocalDate pickupDate = order.getPickupDate();
        LocalTime pickupTime = order.getPickupTime();

        // ⭐️ 여기서 DEFAULT_MAX_SLOTS_PER_TIME 또는 this.defaultMaxSlotsPerTime (주입받은 값)을 사용합니다.
        int maxSlotsPerTime = DEFAULT_MAX_SLOTS_PER_TIME; // 또는 this.defaultMaxSlotsPerTime
        log.debug("[ShopScheduleService] 모든 매장의 픽업 시간대별 기본 최대 슬롯: {}개", maxSlotsPerTime);

        long currentActiveOrders = sellerOrderRepository.countActiveOrdersForPickupTime(
                shopId, pickupDate, pickupTime, EXCLUDED_STATUSES);

        log.debug("[ShopScheduleService] 매장ID: {}, 일시: {} 에 현재 유효 주문: {}개, 최대 슬롯: {}개",
                shopId, pickupDate.atTime(pickupTime), currentActiveOrders, maxSlotsPerTime);

        if (currentActiveOrders >= maxSlotsPerTime) {
            log.error("[ShopScheduleService] ❌ 픽업 시간 슬롯 부족. 주문을 생성할 수 없습니다. 매장ID: {}, 일시: {}",
                    shopId, pickupDate.atTime(pickupTime));
            throw new RuntimeException("픽업 시간 슬롯이 가득 찼습니다. 다른 시간을 선택해주세요.");
        }

        log.info("[ShopScheduleService] ✅ 픽업 시간 슬롯 여유 확인 완료. 주문이 정상적으로 생성됩니다. 남은 가용 슬롯 (이론적): {}",
                (maxSlotsPerTime - (currentActiveOrders + 1)));
        log.info("[ShopScheduleService] <<< decreaseSlotsForOrderCreation 처리 최종 완료.");
        return null;
    }

    // adjustScheduleSlotsForOrderStatusChange 메서드는 이전과 동일하게 동작합니다.
    @Override
    public void adjustScheduleSlotsForOrderStatusChange(CakeOrder order, OrderStatus oldStatus, OrderStatus newStatus) {
        log.info("[ShopScheduleService] ➡️ adjustScheduleSlotsForOrderStatusChange 호출됨 (CakeOrder 기반). 주문ID: {}, 이전상태: {}, 새상태: {}",
                order.getOrderId(), oldStatus, newStatus);
        log.debug("[ShopScheduleService] CakeOrder 테이블 기반 슬롯 관리에서는 별도의 schedule 슬롯 조정이 필요 없습니다.");
        log.info("[ShopScheduleService] <<< adjustScheduleSlotsForOrderStatusChange 처리 최종 완료.");
    }


    }









