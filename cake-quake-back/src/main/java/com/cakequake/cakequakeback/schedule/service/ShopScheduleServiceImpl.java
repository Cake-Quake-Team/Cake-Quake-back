package com.cakequake.cakequakeback.schedule.service;

import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.order.entities.CakeOrder;
import com.cakequake.cakequakeback.order.entities.OrderStatus;
import com.cakequake.cakequakeback.order.repo.SellerOrderRepository;
import com.cakequake.cakequakeback.schedule.dto.ShopOperatingHoursDTO;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
    @Override
    public List<Integer> parseCloseDays(String closeDaysString) {
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
    @Override
    public List<ShopScheduleDTO> getAvailableShops(LocalDate date, LocalTime time, boolean checkSlots) {
        log.info("🏢 getAvailableShops 호출 (date: {}, time: {}, checkSlots: {})", date, time, checkSlots);

        List<ShopScheduleDTO> activeShops = shopRepository.findShopPreviewDTOByStatus(ShopStatus.ACTIVE);
        List<ShopScheduleDTO> availableShops = new ArrayList<>();

        int dayOfWeek = date.getDayOfWeek().getValue();
        LocalTime timeToCheck = (time != null) ? time : LocalTime.now();

        for (ShopScheduleDTO shopDto : activeShops) {
            try {
                List<Integer> closeDays = parseCloseDays(shopDto.getCloseDays());
                if (closeDays.contains(dayOfWeek)) continue;

                LocalTime openTime = LocalTime.parse(shopDto.getOpenTime());
                LocalTime closeTime = LocalTime.parse(shopDto.getCloseTime());

                boolean isOpen;
                if (openTime.isBefore(closeTime)) {
                    isOpen = !(timeToCheck.isBefore(openTime) || timeToCheck.isAfter(closeTime));
                } else { // 자정 넘김
                    isOpen = timeToCheck.isAfter(openTime) || timeToCheck.isBefore(closeTime);
                }
                if (!isOpen) continue;

                if (checkSlots) {
                    LocalDateTime scheduleDateTime = LocalDateTime.of(date, timeToCheck);

                    Optional<ShopSchedule> scheduleOpt = shopScheduleRepository.findByShop_ShopIdAndScheduleDateTime(shopDto.getShopId(), scheduleDateTime);

                    // 🔑 엔티티의 isReservable 메서드로 예약 가능 여부 판단
                    boolean hasAvailableSlots = scheduleOpt.map(ShopSchedule::isReservable)
                            .orElse(true); // 스케줄 없으면 예약 가능으로 처리 (기본 슬롯 있음)

                    if (!hasAvailableSlots) continue;
                }

                availableShops.add(shopDto);

            } catch (DateTimeParseException e) {
                log.error("❌ 영업시간/휴무일 파싱 오류: shopId: {}, 오류: {}", shopDto.getShopId(), e.getMessage());
            } catch (Exception e) {
                log.error("❌ 매장 처리 오류: shopId: {}, 오류: {}", shopDto.getShopId(), e.getMessage());
            }
        }

        log.info("✅ 예약 가능한 매장 수: {}", availableShops.size());
        return availableShops;
    }

    // ⭐ 새로 추가할 메서드 시작 ⭐

    /* 특정 매장의 특정 날짜에 대한 운영 시간 정보를 조회합 */
    public ShopOperatingHoursDTO getShopOperatingHours(Long shopId, LocalDate date) {
        log.info("🗓️ getShopOperatingHours 호출 (shopId: {}, date: {})", shopId, date);

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("Shop not found with ID: " + shopId));

        List<Integer> closeDays = parseCloseDays(shop.getCloseDays());
        boolean isClosedToday = closeDays.contains(date.getDayOfWeek().getValue());

        String message = null;
        if (isClosedToday) {
            message = "매장 휴무일입니다.";
        }

        return ShopOperatingHoursDTO.builder()
                .shopId(shop.getShopId())
                .openTime(shop.getOpenTime().format(DateTimeFormatter.ofPattern("HH:mm"))) // HH:mm 형태로 포맷
                .closeTime(shop.getCloseTime().format(DateTimeFormatter.ofPattern("HH:mm"))) // HH:mm 형태로 포맷
                .isClosed(isClosedToday)
                .message(message)
                .build();
    }

    /* 특정 매장, 특정 날짜에 예약이 가득 찬 (더 이상 예약 불가능한) 시간 목록을 HH:MM 문자열 형식으로 조회 */
    public List<String> getOccupiedTimeSlots(Long shopId, LocalDate date) {
        log.info("🚫 getOccupiedTimeSlots 호출 (shopId: {}, date: {})", shopId, date);

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("Shop not found with ID: " + shopId));

        // 휴무일인 경우 모든 시간대가 예약 불가능한(occupied) 상태로 간주
        List<Integer> closeDays = parseCloseDays(shop.getCloseDays());
        if (closeDays.contains(date.getDayOfWeek().getValue())) {
            log.info("🚫 매장 휴무일. 모든 시간대가 occupied로 간주. shopId: {}, date: {}", shopId, date);
            // 매장 영업 시작 시간부터 종료 시간까지의 모든 가능한 픽업 시간을 occupied로 반환
            return getPossiblePickupTime(shopId).stream()
                    .map(time -> time.format(DateTimeFormatter.ofPattern("HH:mm")))
                    .collect(Collectors.toList());
        }

        List<LocalTime> possiblePickupTimes = getPossiblePickupTime(shopId);
        List<String> occupiedTimes = new ArrayList<>();

        for (LocalTime time : possiblePickupTimes) {
            LocalDateTime scheduleDateTime = LocalDateTime.of(date, time);
            Optional<ShopSchedule> scheduleOpt = shopScheduleRepository.findByShop_ShopIdAndScheduleDateTime(shopId, scheduleDateTime);

            // 스케줄이 존재하고, 예약 가능 슬롯이 없는 경우 (isReservable()이 false인 경우)
            boolean isOccupied = scheduleOpt.map(schedule -> !schedule.isReservable()).orElse(false);
            // isReservable()이 false일 때만 occupied로 간주합니다.
            // 스케줄이 아예 없는 시간대는 isReservable()이 기본적으로 true로 간주된다면 occupied가 아닙니다.
            // (ShopSchedule 엔티티의 isReservable 구현에 따라 달라짐)

            if (isOccupied) {
                occupiedTimes.add(time.format(DateTimeFormatter.ofPattern("HH:mm")));
            }
        }

        log.info("🚫 예약된 시간 슬롯 개수: {}", occupiedTimes.size());
        return occupiedTimes;
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
    @Override
    public void decreaseSlotsForOrderCreation(Long shopId, LocalDate pickupDate, LocalTime pickupTime) {
        log.info("🔽 픽업 슬롯 감소 요청 (shopId: {}, pickupDate: {}, pickupTime: {})", shopId, pickupDate, pickupTime);

        // 1. 매장 존재 여부 확인
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> {
                    log.error("❌ Shop ID {} 를 찾을 수 없어 픽업 슬롯 감소 실패.", shopId);
                    return new BusinessException(ErrorCode.NOT_FOUND_SHOP_ID, "매장 정보를 찾을 수 없습니다.");
                });

        // 2. 해당 날짜가 매장 휴무일인지 확인
        List<Integer> closeDays = parseCloseDays(shop.getCloseDays());
        if (closeDays.contains(pickupDate.getDayOfWeek().getValue())) {
            log.warn("⚠️ 픽업 날짜 {}는 매장 {}의 휴무일입니다. 픽업 슬롯 감소 불가.", pickupDate, shopId);
            throw new BusinessException(ErrorCode.PICKUP_SLOT_UNAVAILABLE, "선택하신 픽업 날짜는 매장 휴무일입니다.");
        }

        // 3. 픽업 시간이 매장 운영 시간 범위 내인지 확인
        LocalTime openTime = shop.getOpenTime();
        LocalTime closeTime = shop.getCloseTime();
        // 자정을 넘어가는 운영시간 처리 (예: 22:00 ~ 02:00)
        boolean isDuringOperatingHours;
        if (openTime.isBefore(closeTime)) { // 22:00 ~ 23:00
            isDuringOperatingHours = !(pickupTime.isBefore(openTime) || pickupTime.isAfter(closeTime));
        } else { // 22:00 ~ 02:00 (다음날)
            // 픽업 시간이 오픈 시간보다 늦거나 (당일 밤)
            // 픽업 시간이 마감 시간보다 이르면 (다음날 새벽)
            isDuringOperatingHours = pickupTime.isAfter(openTime) || pickupTime.isBefore(closeTime) || pickupTime.equals(openTime);
        }

        if (!isDuringOperatingHours) {
            log.warn("⚠️ 픽업 시간 {}는 매장 {}의 운영 시간({}~{}) 범위 밖입니다. 픽업 슬롯 감소 불가.", pickupTime, shopId, openTime, closeTime);
            throw new BusinessException(ErrorCode.PICKUP_SLOT_UNAVAILABLE, "선택하신 픽업 시간은 매장의 운영 시간이 아닙니다.");
        }

        // 4. ShopSchedule 엔티티 조회 또는 생성
        LocalDateTime scheduleDateTime = LocalDateTime.of(pickupDate, pickupTime);

        ShopSchedule shopSchedule = shopScheduleRepository.findByShop_ShopIdAndScheduleDateTime(shopId, scheduleDateTime)
                .orElseGet(() -> {
                    // 스케줄 엔티티가 없으면 새로 생성 (기본 최대 슬롯으로 초기화)
                    ShopSchedule newSchedule = ShopSchedule.builder()
                            .shop(shop)
                            .scheduleDateTime(scheduleDateTime)
                            .maxSlots(DEFAULT_MAX_SLOTS_PER_TIME)
                            .availableSlots(DEFAULT_MAX_SLOTS_PER_TIME)
                            .build();
                    log.info("➕ 새로운 ShopSchedule 엔티티 생성: shopId={}, dateTime={}", shopId, scheduleDateTime);
                    return newSchedule;
                });

        // 5. 슬롯 감소 시도
        if (shopSchedule.getAvailableSlots() <= 0) {
            log.warn("🚫 매장 {}의 픽업 시간 {}에 잔여 슬롯이 없습니다. (현재: {})", shopId, scheduleDateTime, shopSchedule.getAvailableSlots());
            throw new BusinessException(ErrorCode.PICKUP_SLOT_UNAVAILABLE, "선택하신 픽업 시간은 예약이 마감되었습니다.");
        }

        shopSchedule.decreaseAvailableSlots(1); // 슬롯 1 감소
        shopScheduleRepository.save(shopSchedule); // 변경사항 저장

        log.info("✅ 매장 {}의 {} 픽업 슬롯 1 감소. 잔여 슬롯: {}", shopId, scheduleDateTime, shopSchedule.getAvailableSlots());
    }

}
