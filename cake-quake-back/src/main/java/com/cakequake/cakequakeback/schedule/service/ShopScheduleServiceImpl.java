package com.cakequake.cakequakeback.schedule.service;

import com.cakequake.cakequakeback.order.entities.CakeOrder;
import com.cakequake.cakequakeback.order.entities.OrderStatus;
import com.cakequake.cakequakeback.order.repo.SellerOrderRepository;
import com.cakequake.cakequakeback.schedule.entities.ShopSchedule;
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

    private static final List<OrderStatus> EXCLUDED_STATUSES = Arrays.asList(
            OrderStatus.RESERVATION_CANCELLED,
            OrderStatus.NO_SHOW
    );

    private static final Map<String, Integer> KOREAN_DAY_OF_WEEK_MAP = new HashMap<>();
    static {
        KOREAN_DAY_OF_WEEK_MAP.put("월요일", DayOfWeek.MONDAY.getValue());
        KOREAN_DAY_OF_WEEK_MAP.put("화요일", DayOfWeek.TUESDAY.getValue());
        KOREAN_DAY_OF_WEEK_MAP.put("수요일", DayOfWeek.WEDNESDAY.getValue());
        KOREAN_DAY_OF_WEEK_MAP.put("목요일", DayOfWeek.THURSDAY.getValue());
        KOREAN_DAY_OF_WEEK_MAP.put("금요일", DayOfWeek.FRIDAY.getValue());
        KOREAN_DAY_OF_WEEK_MAP.put("토요일", DayOfWeek.SATURDAY.getValue());
        KOREAN_DAY_OF_WEEK_MAP.put("일요일", DayOfWeek.SUNDAY.getValue());
        // 필요하다면 "월", "화" 등 약어도 추가할 수 있습니다.
        KOREAN_DAY_OF_WEEK_MAP.put("월", DayOfWeek.MONDAY.getValue());
        KOREAN_DAY_OF_WEEK_MAP.put("화", DayOfWeek.TUESDAY.getValue());
        KOREAN_DAY_OF_WEEK_MAP.put("수", DayOfWeek.WEDNESDAY.getValue());
        KOREAN_DAY_OF_WEEK_MAP.put("목", DayOfWeek.THURSDAY.getValue());
        KOREAN_DAY_OF_WEEK_MAP.put("금", DayOfWeek.FRIDAY.getValue());
        KOREAN_DAY_OF_WEEK_MAP.put("토", DayOfWeek.SATURDAY.getValue());
        KOREAN_DAY_OF_WEEK_MAP.put("일", DayOfWeek.SUNDAY.getValue());
    }


    private List<Integer> parseCloseDays(String closeDaysString) {
        if (closeDaysString == null || closeDaysString.trim().isEmpty()) {
            return Collections.emptyList();
        }

        // 콤마(,)로 구분된 문자열을 분리
        return Arrays.stream(closeDaysString.split(","))
                .map(String::trim) // 각 요일 문자열의 앞뒤 공백 제거
                .map(dayName -> {
                    // 한글 요일 이름을 정수 값으로 매핑
                    Integer dayValue = KOREAN_DAY_OF_WEEK_MAP.get(dayName);
                    if (dayValue == null) {
                        log.warn("⚠️ 알 수 없는 휴무 요일 이름입니다: '{}'. 이 요일은 무시됩니다.", dayName);
                    }
                    return dayValue; // 매핑된 정수 값, 없으면 null
                })
                .filter(Objects::nonNull) // null 값(매핑되지 않은 요일) 필터링
                .collect(Collectors.toList());
    }

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

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("Shop not found with ID: " + shopId));

        // 여기서 parseCloseDays 헬퍼 메서드를 사용
        List<Integer> parsedCloseDays = parseCloseDays(shop.getCloseDays());

        if (parsedCloseDays.contains(date.getDayOfWeek().getValue())) { // 이제 List<Integer>에 contains 사용
            log.info("📅 매장 {} (ID: {})은 {}에 휴무일입니다. 이용 가능한 픽업 시간이 없습니다.", shop.getShopName(), shopId, date);
            return Collections.emptyList();
        }

        int maxSlotsPerTime = DEFAULT_MAX_SLOTS_PER_TIME;
        log.debug("📅 모든 매장의 픽업 시간대별 기본 최대 슬롯: {}개", maxSlotsPerTime);

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

    @Override
    public List<ShopScheduleDTO> getAvailableShopsByDate(LocalDate date) {
        log.info("🏢 getAvailableShopsByDate 호출 (시나리오 2: DTO Projection 사용). (date: {})", date);

        List<ShopScheduleDTO> activeShops = shopRepository.findShopPreviewDTOByStatus(ShopStatus.ACTIVE);
        List<ShopScheduleDTO> availableShops = new ArrayList<>();

        for (ShopScheduleDTO shopDto : activeShops) { // DTO 객체를 순회
            try {
                // 2. 휴무일 확인 (DTO에서 필드 사용)
                List<Integer> closeDays = parseCloseDays(shopDto.getCloseDays());
                int dayOfWeekValue = date.getDayOfWeek().getValue();

                if (closeDays.contains(dayOfWeekValue)) {
                    continue;
                }

                // 3. 영업 시간 확인 (DTO에서 필드 사용)
                LocalTime requestTime = LocalTime.now();
                LocalTime openTime = LocalTime.parse(shopDto.getOpenTime());
                LocalTime closeTime = LocalTime.parse(shopDto.getCloseTime());

                if (openTime.isBefore(closeTime)) {
                    if (requestTime.isBefore(openTime) || requestTime.isAfter(closeTime)) {
                        continue;
                    }
                } else {
                    if (!(requestTime.isAfter(openTime) || requestTime.isBefore(closeTime))) {
                        continue;
                    }
                }

                // 모든 조건을 통과하면 사용 가능한 매장에 추가
                availableShops.add(shopDto); // 이미 DTO이므로 추가 변환 불필요

            } catch (DateTimeParseException e) {
                log.error("❌ 영업 시간 또는 휴무일 파싱 오류: Shop ID {}, OpenTime: {}, CloseTime: {}, CloseDays: {}. 오류: {}",
                        shopDto.getShopId(), shopDto.getOpenTime(), shopDto.getCloseTime(), shopDto.getCloseDays(), e.getMessage());
            } catch (Exception e) {
                log.error("❌ Shop ID {} 처리 중 알 수 없는 오류 발생: {}", shopDto.getShopId(), e.getMessage(), e);
            }
        }

        log.info("✅ getAvailableShopsByDate 완료. 최종 사용 가능한 매장 수: {}", availableShops.size());
        return availableShops;
    }


    @Override
    public ShopSchedule decreaseSlotsForOrderCreation(CakeOrder order) {
        log.info("[ShopScheduleService] ➡️ decreaseSlotsForOrderCreation 호출 (Shop 엔티티 필드 없이). 주문ID: {}, 픽업일시: {}",
                order.getOrderId(), order.getPickupDate().atTime(order.getPickupTime()));

        Long shopId = order.getShop().getShopId();
        LocalDate pickupDate = order.getPickupDate();
        LocalTime pickupTime = order.getPickupTime();

        int maxSlotsPerTime = DEFAULT_MAX_SLOTS_PER_TIME;
        log.debug("[ShopScheduleService] 모든 매장의 픽업 시간대별 기본 최대 슬롯: {}개", maxSlotsPerTime);

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("Shop not found with ID: " + shopId));

        // 여기서도 parseCloseDays 헬퍼 메서드를 사용
        List<Integer> parsedCloseDays = parseCloseDays(shop.getCloseDays());

        if (parsedCloseDays.contains(pickupDate.getDayOfWeek().getValue())) {
            log.error("[ShopScheduleService] ❌ 픽업일 {}이 매장 휴무일입니다. 주문을 생성할 수 없습니다. 매장ID: {}", pickupDate, shopId);
            throw new RuntimeException("선택하신 픽업일은 매장의 휴무일입니다. 다른 날짜를 선택해주세요.");
        }

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









