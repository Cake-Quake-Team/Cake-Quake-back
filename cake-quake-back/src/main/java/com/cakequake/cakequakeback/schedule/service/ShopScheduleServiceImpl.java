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
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ShopScheduleServiceImpl implements ShopScheduleService {
    private final ShopRepository shopRepository;
    private final ShopScheduleRepository shopScheduleRepository;
    private final MemberRepository memberRepository;
    private final SellerOrderRepository sellerOrderRepository;

    //가능한 모든 픽업 시간
    @Override
    public List<LocalTime> getPossiblePickupTime(Long shopId){
        List<LocalTime> possibleTimes = new ArrayList<>();
        LocalTime startTime = LocalTime.of(5, 0);
        LocalTime endTime = LocalTime.of(23, 0);
        int intervalMinutes = 30;

        LocalTime currentTime = startTime;
        while (currentTime.isBefore(endTime) || currentTime.equals(endTime)) {
            possibleTimes.add(currentTime);
            currentTime = currentTime.ofSecondOfDay(currentTime.toSecondOfDay() + (intervalMinutes * 30)); // 다음 시간으로 이동
        }
        return possibleTimes;

    }

    //특정 매장과 날짜의 예약 가능한 픽업 시간 목록
    @Override
    public List<LocalTime> getAvailablePickupTimes(Long shopId, LocalDate date){
        //해당 매장의 가능한 픽업 시간대
        List<LocalTime> allPossiblePickupTimes = getPossiblePickupTime(shopId);

        //해당 매장, 시간대에 이미 예약된 주문의 픽업시간
        List<OrderStatus> excludedStatuses = List.of(OrderStatus.RESERVATION_CANCELLED, OrderStatus.NO_SHOW);
        List<CakeOrder> existingOrders = sellerOrderRepository.findSchedule(
                shopId, date, excludedStatuses
        );

        //이미 예약된 시간 변환
        Set<LocalTime> occupiedTimes = existingOrders.stream()
                .map(CakeOrder::getPickupTime)
                .collect(Collectors.toSet());

        //사용 가능한 시간 목록 반환
        List<LocalTime> availableTimes =allPossiblePickupTimes.stream()
                .filter(time -> !occupiedTimes.contains(time))
                .collect(Collectors.toList());

        return availableTimes;

    }

    //예약 가능한 매장 목록 조회
    public List<Shop> getAvailableShops(LocalDate date, LocalTime time) {
        List<Shop> allShops = shopRepository.findAll(); // 모든 매장 가져오기
        List<Shop> availableShops = new ArrayList<>();

        for(Shop shop : allShops) {
            List<LocalTime> availableTimesForShop = getAvailablePickupTimes(shop.getShopId(), date);
            if (availableTimesForShop.contains(time)) {
                availableShops.add(shop);
            }
        }
        return availableShops;

    }








}
