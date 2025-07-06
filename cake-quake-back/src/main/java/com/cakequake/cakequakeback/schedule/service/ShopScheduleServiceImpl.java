package com.cakequake.cakequakeback.schedule.service;

import com.cakequake.cakequakeback.order.entities.CakeOrder;
import com.cakequake.cakequakeback.order.entities.OrderStatus;
import com.cakequake.cakequakeback.order.repo.SellerOrderRepository;
import com.cakequake.cakequakeback.schedule.repo.ShopScheduleRepository;
import com.cakequake.cakequakeback.schedule.dto.ShopScheduleDTO;
import com.cakequake.cakequakeback.shop.entities.Shop;
import com.cakequake.cakequakeback.shop.entities.ShopStatus;
import com.cakequake.cakequakeback.shop.repo.ShopRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
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

    private static final int DEFAULT_MAX_SLOTS_PER_TIME = 10;
    private static final int TEMP_LARGE_PAGE_SIZE = 1000;

    // 🚩 주문 상태 값 중 제외할 상태들
    private static final List<OrderStatus> EXCLUDED_STATUSES = Arrays.asList(
            OrderStatus.RESERVATION_CANCELLED,
            OrderStatus.NO_SHOW
    );

    // 🚩 한글 요일 이름 → 숫자 매핑 (휴무일 판단용)
    private static final Map<String, Integer> KOREAN_DAY_OF_WEEK_MAP = new HashMap<>();
    static {
        KOREAN_DAY_OF_WEEK_MAP.put("월요일", DayOfWeek.MONDAY.getValue());
        KOREAN_DAY_OF_WEEK_MAP.put("화요일", DayOfWeek.TUESDAY.getValue());
        KOREAN_DAY_OF_WEEK_MAP.put("수요일", DayOfWeek.WEDNESDAY.getValue());
        KOREAN_DAY_OF_WEEK_MAP.put("목요일", DayOfWeek.THURSDAY.getValue());
        KOREAN_DAY_OF_WEEK_MAP.put("금요일", DayOfWeek.FRIDAY.getValue());
        KOREAN_DAY_OF_WEEK_MAP.put("토요일", DayOfWeek.SATURDAY.getValue());
        KOREAN_DAY_OF_WEEK_MAP.put("일요일", DayOfWeek.SUNDAY.getValue());
        KOREAN_DAY_OF_WEEK_MAP.put("월", DayOfWeek.MONDAY.getValue());
        KOREAN_DAY_OF_WEEK_MAP.put("화", DayOfWeek.TUESDAY.getValue());
        KOREAN_DAY_OF_WEEK_MAP.put("수", DayOfWeek.WEDNESDAY.getValue());
        KOREAN_DAY_OF_WEEK_MAP.put("목", DayOfWeek.THURSDAY.getValue());
        KOREAN_DAY_OF_WEEK_MAP.put("금", DayOfWeek.FRIDAY.getValue());
        KOREAN_DAY_OF_WEEK_MAP.put("토", DayOfWeek.SATURDAY.getValue());
        KOREAN_DAY_OF_WEEK_MAP.put("일", DayOfWeek.SUNDAY.getValue());
    }

    /**
     * 🔹 휴무일 문자열(한글 요일) → 숫자 리스트 변환
     */
    private List<Integer> parseCloseDays(String closeDaysString) {
        if (closeDaysString == null || closeDaysString.trim().isEmpty()) {
            return Collections.emptyList();
        }

        return Arrays.stream(closeDaysString.split(","))
                .map(String::trim)
                .map(dayName -> {
                    Integer dayValue = KOREAN_DAY_OF_WEEK_MAP.get(dayName);
                    if (dayValue == null) {
                        log.warn("⚠️ 알 수 없는 휴무 요일: '{}'", dayName);
                    }
                    return dayValue;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 🔹 특정 매장의 가능한 모든 픽업 시간 조회 (운영시간 기반)
     */
    @Override
    public List<LocalTime> getPossiblePickupTime(Long shopId) {
        log.info("⏰ getPossiblePickupTime 호출. shopId: {}", shopId);

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> {
                    log.error("⏰ Shop ID {} 찾을 수 없음.", shopId);
                    return new IllegalArgumentException("Shop not found with ID: " + shopId);
                });

        LocalTime startTime = shop.getOpenTime();
        LocalTime endTime = shop.getCloseTime();
        int intervalMinutes = 30;

        List<LocalTime> possibleTimes = new ArrayList<>();
        LocalTime currentTime = startTime;
        while (!currentTime.isAfter(endTime)) {
            possibleTimes.add(currentTime);
            currentTime = currentTime.plusMinutes(intervalMinutes);
        }

        log.info("⏰ 가능 픽업 시간 개수: {}", possibleTimes.size());
        return possibleTimes;
    }

    /**
     * 🔹 특정 매장, 특정 날짜에 예약 가능한 시간 목록 조회
     */
    @Override
    public List<LocalTime> getAvailablePickupTimes(Long shopId, LocalDate date) {
        log.info("📅 getAvailablePickupTimes 호출 (shopId: {}, date: {})", shopId, date);

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("Shop not found with ID: " + shopId));

        List<Integer> closeDays = parseCloseDays(shop.getCloseDays());
        if (closeDays.contains(date.getDayOfWeek().getValue())) {
            log.info("📅 매장 휴무일. shopId: {}, date: {}", shopId, date);
            return Collections.emptyList();
        }

        List<LocalTime> possiblePickupTimes = getPossiblePickupTime(shopId);
        List<LocalTime> availableTimes = new ArrayList<>();

        for (LocalTime time : possiblePickupTimes) {
            LocalDateTime scheduleDateTime = LocalDateTime.of(date, time);

            int remainingSlots = shopScheduleRepository.findByShop_ShopIdAndScheduleDateTime(shopId, scheduleDateTime)
                    .map(schedule -> schedule.getAvailableSlots())
                    .orElse(DEFAULT_MAX_SLOTS_PER_TIME);

            if (remainingSlots > 0) {
                availableTimes.add(time);
            }
        }

        availableTimes.sort(Comparator.naturalOrder());
        log.info("✅ 사용 가능한 시간 수: {}", availableTimes.size());
        return availableTimes;
    }

    /**
     * 🔹 특정 날짜에 예약 가능한 매장 목록 조회
     */
    @Override
    public List<ShopScheduleDTO> getAvailableShopsByDate(LocalDate date) {
        log.info("🏢 getAvailableShopsByDate 호출 (date: {})", date);

        List<ShopScheduleDTO> activeShops = shopRepository.findShopPreviewDTOByStatus(ShopStatus.ACTIVE);
        List<ShopScheduleDTO> availableShops = new ArrayList<>();

        for (ShopScheduleDTO shopDto : activeShops) {
            try {
                List<Integer> closeDays = parseCloseDays(shopDto.getCloseDays());
                int dayOfWeek = date.getDayOfWeek().getValue();
                if (closeDays.contains(dayOfWeek)) continue;

                LocalTime now = LocalTime.now();
                LocalTime openTime = LocalTime.parse(shopDto.getOpenTime());
                LocalTime closeTime = LocalTime.parse(shopDto.getCloseTime());

                if (openTime.isBefore(closeTime)) {
                    if (now.isBefore(openTime) || now.isAfter(closeTime)) continue;
                } else {
                    if (!(now.isAfter(openTime) || now.isBefore(closeTime))) continue;
                }

                availableShops.add(shopDto);

            } catch (DateTimeParseException e) {
                log.error("❌ 영업시간/휴무일 파싱 오류: shopId: {}, 오류: {}", shopDto.getShopId(), e.getMessage());
            } catch (Exception e) {
                log.error("❌ 매장 처리 중 오류: shopId: {}, 오류: {}", shopDto.getShopId(), e.getMessage());
            }
        }

        log.info("✅ 예약 가능한 매장 수: {}", availableShops.size());
        return availableShops;
    }

    /**
     * 🔹 특정 날짜+시간에 예약 가능한 매장 목록 조회
     */
    @Override
    public List<ShopScheduleDTO> getAvailableShopsByDateAndTime(LocalDate date, LocalTime time) {
        List<ShopScheduleDTO> activeShops = shopRepository.findShopPreviewDTOByStatus(ShopStatus.ACTIVE);
        List<ShopScheduleDTO> availableShops = new ArrayList<>();

        for (ShopScheduleDTO shopDto : activeShops) {
            try {
                List<Integer> closeDays = parseCloseDays(shopDto.getCloseDays());
                if (closeDays.contains(date.getDayOfWeek().getValue())) continue;

                LocalTime openTime = LocalTime.parse(shopDto.getOpenTime());
                LocalTime closeTime = LocalTime.parse(shopDto.getCloseTime());
                if (time.isBefore(openTime) || time.isAfter(closeTime)) continue;

                LocalDateTime scheduleDateTime = LocalDateTime.of(date, time);
                int remainingSlots = shopScheduleRepository.findByShop_ShopIdAndScheduleDateTime(shopDto.getShopId(), scheduleDateTime)
                        .map(schedule -> schedule.getAvailableSlots())
                        .orElse(DEFAULT_MAX_SLOTS_PER_TIME);

                if (remainingSlots > 0) {
                    availableShops.add(shopDto);
                }
            } catch (Exception e) {
                log.error("❌ 매장 처리 오류: shopId: {}, 오류: {}", shopDto.getShopId(), e.getMessage());
            }
        }

        return availableShops;
    }

    /**
     * 🔹 주문 상태 변경에 따른 예약 슬롯 자동 조정
     */
    @Override
    public void adjustScheduleSlotsForOrderStatusChange(CakeOrder order, OrderStatus newStatus) {
        Long shopId = order.getShop().getShopId();
        LocalDateTime scheduleDateTime = LocalDateTime.of(order.getPickupDate(), order.getPickupTime());

        shopScheduleRepository.findByShop_ShopIdAndScheduleDateTime(shopId, scheduleDateTime)
                .ifPresent(schedule -> {
                    if (newStatus == OrderStatus.RESERVATION_CANCELLED) {
                        schedule.increaseAvailableSlots(1);
                    } else if (newStatus == OrderStatus.RESERVATION_CONFIRMED || newStatus == OrderStatus.PREPARING) {
                        schedule.decreaseAvailableSlots(1);
                    }
                    shopScheduleRepository.save(schedule);
                });
    }
}
