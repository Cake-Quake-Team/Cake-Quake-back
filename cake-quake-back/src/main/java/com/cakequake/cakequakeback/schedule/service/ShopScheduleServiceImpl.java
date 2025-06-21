package com.cakequake.cakequakeback.schedule.service;

import com.cakequake.cakequakeback.member.repo.MemberRepository;
import com.cakequake.cakequakeback.order.entities.CakeOrder;
import com.cakequake.cakequakeback.order.entities.OrderStatus;
import com.cakequake.cakequakeback.order.repo.SellerOrderRepository;
import com.cakequake.cakequakeback.schedule.repo.ShopScheduleRepository;
import com.cakequake.cakequakeback.shop.entities.Shop;
import com.cakequake.cakequakeback.shop.repo.ShopRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ShopScheduleServiceImpl implements ShopScheduleService {
    private final ShopRepository shopRepository;
    private final SellerOrderRepository sellerOrderRepository;

    //가능한 모든 픽업 시간
    @Override
    public List<LocalTime> getPossiblePickupTime(Long shopId) {
        log.info("⏰ getPossiblePickupTime 호출. (shopId: {})", shopId);

        // shopId를 사용하여 해당 매장 정보를 조회
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
            // 올바른 시간 계산 (plusMinutes 사용)
            currentTime = currentTime.plusMinutes(intervalMinutes);
        }

        log.info("⏰ getPossiblePickupTime 완료. 생성된 가능 시간 수: {}", possibleTimes.size());
        log.debug("⏰ 생성된 모든 가능 시간: {}", possibleTimes);
        return possibleTimes;
    }


    //특정 매장과 날짜의 예약 가능한 픽업 시간 목록
    @Override
    public List<LocalTime> getAvailablePickupTimes(Long shopId, LocalDate date){
        log.info("📅 getAvailablePickupTimes 호출. (shopId: {}, date: {})", shopId, date);

        //해당 매장의 가능한 픽업 시간대
        List<LocalTime> allPossiblePickupTimes = getPossiblePickupTime(shopId);
        log.debug("📅 모든 가능한 픽업 시간: {}", allPossiblePickupTimes);

        //해당 매장, 시간대에 이미 예약된 주문의 픽업시간
        List<OrderStatus> excludedStatuses = List.of(OrderStatus.RESERVATION_CANCELLED, OrderStatus.NO_SHOW);
        log.debug("📅 제외할 주문 상태: {}", excludedStatuses);

        List<CakeOrder> existingOrders = sellerOrderRepository.findSchedule(
                shopId, date, excludedStatuses
        );
        log.info("📅 DB에서 조회된 기존 예약 건수 (shopId: {}, date: {}): {}", shopId, date, existingOrders.size());
        log.debug("📅 조회된 기존 예약 상세: {}", existingOrders);

        //이미 예약된 시간 변환
        Set<LocalTime> occupiedTimes = existingOrders.stream()
                .map(CakeOrder::getPickupTime)
                .collect(Collectors.toSet());
        log.info("📅 점유된 픽업 시간 수: {}", occupiedTimes.size());
        log.debug("📅 점유된 픽업 시간: {}", occupiedTimes);

        //사용 가능한 시간 목록 반환
        List<LocalTime> availableTimes = allPossiblePickupTimes.stream()
                .filter(time -> {
                    boolean isOccupied = occupiedTimes.contains(time);
                    if (log.isDebugEnabled()) {
                        log.debug("📅 시간 {} - 점유 여부: {}. 점유된 시간 목록에 포함: {}", time, isOccupied, occupiedTimes.contains(time));
                    }
                    return !isOccupied;
                })
                .collect(Collectors.toList());

        log.info("✅ getAvailablePickupTimes 완료. 최종 사용 가능한 시간 수: {}", availableTimes.size());
        log.debug("✅ 최종 사용 가능한 시간 목록: {}", availableTimes);
        return availableTimes;

    }

    //예약 가능한 매장 목록 조회
    public List<Shop> getAvailableShops(LocalDate date, LocalTime time) {
        log.info("🏢 getAvailableShops 호출. (date: {}, time: {})", date, time);

        List<Shop> allShops = shopRepository.findAll(); // 모든 매장 가져오기
        log.info("🏢 전체 매장 수: {}", allShops.size());
        log.debug("🏢 전체 매장 목록: {}", allShops);

        List<Shop> availableShops = new ArrayList<>();

        for(Shop shop : allShops) {
            log.debug("🏢 매장 {} ({})에 대한 가능 시간 확인 시작...", shop.getShopId(), shop.getShopName());
            List<LocalTime> availableTimesForShop = getAvailablePickupTimes(shop.getShopId(), date);
            log.debug("🏢 매장 {} ({})의 날짜 {}에 사용 가능한 시간: {}", shop.getShopName(), shop.getShopId(), date, availableTimesForShop);

            if (availableTimesForShop.contains(time)) {
                log.info("🏢 매장 {} ({})가 시간 {}에 예약 가능합니다. 추가.", shop.getShopName(), shop.getShopId(), time);
                availableShops.add(shop);
            } else {
                log.debug("🏢 매장 {} ({})는 시간 {}에 예약 불가능합니다. (가능 시간: {})", shop.getShopName(), shop.getShopId(), time, availableTimesForShop);
            }
        }
        log.info("✅ getAvailableShops 완료. 최종 사용 가능한 매장 수: {}", availableShops.size());
        log.debug("✅ 최종 사용 가능한 매장 목록: {}", availableShops);
        return availableShops;

    }








}
