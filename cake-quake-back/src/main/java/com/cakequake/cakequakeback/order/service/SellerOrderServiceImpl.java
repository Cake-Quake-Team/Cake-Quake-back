package com.cakequake.cakequakeback.order.service;

import com.cakequake.cakequakeback.badge.repo.BadgeRepository;
import com.cakequake.cakequakeback.badge.repo.MemberBadgeRepository;
import com.cakequake.cakequakeback.badge.service.BadgeService;
import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.member.repo.MemberRepository;
import com.cakequake.cakequakeback.notification.entities.NotificationType;
import com.cakequake.cakequakeback.notification.service.NotificationService;
import com.cakequake.cakequakeback.notification.service.PickupReminderSchedulingService;
import com.cakequake.cakequakeback.order.dto.seller.SellerOrderDetail;
import com.cakequake.cakequakeback.order.dto.seller.SellerOrderList;
import com.cakequake.cakequakeback.order.dto.seller.SellerStatistics;
import com.cakequake.cakequakeback.order.entities.CakeOrder;
import com.cakequake.cakequakeback.order.entities.CakeOrderItem;
import com.cakequake.cakequakeback.order.entities.CakeOrderItemOption;
import com.cakequake.cakequakeback.order.entities.OrderStatus;
import com.cakequake.cakequakeback.order.repo.CakeOrderItemOptionRepository;
import com.cakequake.cakequakeback.order.repo.CakeOrderItemRepository;
import com.cakequake.cakequakeback.order.repo.SellerOrderRepository;

import com.cakequake.cakequakeback.schedule.service.ShopScheduleService;
import com.cakequake.cakequakeback.temperature.service.TemperatureService;

import com.cakequake.cakequakeback.point.service.PointService;
import com.cakequake.cakequakeback.temperature.entities.Grade;
import com.cakequake.cakequakeback.temperature.entities.Temperature;
import com.cakequake.cakequakeback.temperature.repo.TemperatureRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.lang.Nullable;

import java.io.ByteArrayOutputStream; // PDF 생성용 임포트
import java.io.IOException; // PDF 생성용 임포트
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class SellerOrderServiceImpl implements SellerOrderService {

    private final SellerOrderRepository sellerOrderRepository;
    private final CakeOrderItemRepository cakeOrderItemRepository;
    private final CakeOrderItemOptionRepository cakeOrderItemOptionRepository;

    private final TemperatureService temperatureService;

    private final PointService pointService;
    private final TemperatureRepository temperatureRepository;
    private final PickupReminderSchedulingService pickupReminderSchedulingService;
    private final NotificationService notificationService;

    private final ShopScheduleService shopScheduleService;

    private final MemberRepository memberRepository;
    private final MemberBadgeRepository memberBadgeRepository;
    private final BadgeRepository badgeRepository;
    private final BadgeService badgeService;


    //특정 가게(shopId)에 대한 주문 리스트를 페이징 처리하여 조회
    @Override
    public SellerOrderList.Response getShopOrderList(Long shopId, Pageable pageable, @Nullable OrderStatus status) { // @Nullable 어노테이션 추가
        // 1) 가게 ID로 CakeOrder 엔티티를 페이징 조회
        Page<CakeOrder> page;
        // status 파라미터에 따라 호출할 리포지토리 메서드를 분기
        if (status != null) {
            page = sellerOrderRepository.findByShopIdAndStatus(shopId, status, pageable);
        } else {
            // status가 null이면 (즉, 모든 상태의 주문을 조회할 때) 기존 findByShopId 사용
            page = sellerOrderRepository.findByShopId(shopId, pageable);
        } // 2) 조회된 CakeOrder 목록을 SellerOrderListItem DTO로 변환
        List<SellerOrderList.Response.SellerOrderListItem> dtoItems = page.getContent().stream()
                .map(order -> {
                    List<CakeOrderItem> orderItems = cakeOrderItemRepository.findByCakeOrder_OrderId(order.getOrderId());
                    String cname = "상품 정보 없음";
                    String thumbnail = null;
                    Integer cnt = 0;

                    if (!orderItems.isEmpty()) {
                        CakeOrderItem firstItem = orderItems.get(0);
                        cname = firstItem.getCakeItem().getCname();
                        thumbnail = firstItem.getCakeItem().getThumbnailImageUrl();
                        cnt = firstItem.getQuantity();
                    }

                    return SellerOrderList.Response.SellerOrderListItem.builder()
                            .orderId(order.getOrderId())
                            .orderNumber(order.getOrderNumber())
                            .cname(cname)
                            .thumbnailImageUrl(thumbnail)
                            .pickupDate(order.getPickupDate())
                            .pickupTime(order.getPickupTime())
                            .status(order.getStatus().name())
                            .productCnt(cnt)
                            .OrderTotalPrice(order.getOrderTotalPrice())
                            .discountAmount(order.getDiscountAmount()) // 추가
                            .finalPaymentAmount(order.getFinalPaymentAmount()) // 추가
                            .build();
                })
                .collect(Collectors.toList());

        // 3) 페이지 정보(PageInfo) DTO 생성: 현재 페이지 번호, 전체 페이지 수, 전체 요소 개수
        SellerOrderList.Response.PageInfo pageInfo = SellerOrderList.Response.PageInfo.builder()
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .build();

        // 4) 최종 응답 DTO에 목록과 페이지 정보를 담아서 반환
        return SellerOrderList.Response.builder()
                .orders(dtoItems)
                .pageInfo(pageInfo)
                .build();
    }

    //특정 가게(shopId)에 속한 단일 주문(orderId)의 상세 정보를 조회
    @Override
    public SellerOrderDetail.Response getShopOrderDetail(Long shopId, Long orderId) {
        // 1. CakeOrder 정보 조회: Member와 Shop을 FETCH JOIN으로 함께 가져와 N+1 방지
        // sellerOrderRepository에 findByOrderIdAndShopIdWithShopAndMember 메서드를 추가했다고 가정합니다.
        CakeOrder order = sellerOrderRepository
                .findByOrderIdAndShopId(orderId, shopId) // 수정: 새로운 Repository 메서드 사용
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ORDER_ID));

        // 2. 모든 CakeOrderItem 목록 조회: CakeItem 정보도 함께 FETCH JOIN으로 가져와 N+1 방지
        // 수정: cakeOrderItemRepository의 findByCakeOrder_OrderIdWithCakeItem 메서드 사용
        List<CakeOrderItem> orderItems = cakeOrderItemRepository.findByCakeOrder_OrderIdWithCakeItem(orderId);

        // 3. 모든 CakeOrderItem의 ID를 추출
        List<Long> orderItemIds = orderItems.stream()
                .map(CakeOrderItem::getOrderItemId)
                .collect(Collectors.toList());

        // 4. 추출된 ID를 이용해 모든 CakeOrderItemOption을 한 번에 조회 (핵심 N+1 해결)
        List<CakeOrderItemOption> allItemOptions = new ArrayList<>();
        if (!orderItemIds.isEmpty()) {
            // 수정: CakeOrderItemOptionRepository의 findByCakeOrderItem_OrderItemIdIn 메서드 사용 (OptionItem까지 JOIN FETCH)
            allItemOptions = cakeOrderItemOptionRepository.findByCakeOrderItem_OrderItemIdIn(orderItemIds);
        }

        // 5. 조회된 옵션들을 orderItemId를 기준으로 그룹화하여 Map으로 준비
        Map<Long, List<CakeOrderItemOption>> optionsByOrderItemId = allItemOptions.stream()
                .collect(Collectors.groupingBy(option -> option.getCakeOrderItem().getOrderItemId()));

        // 6. ProductDetail DTO 리스트 생성 및 각 ProductDetail에 옵션 정보 추가
        List<SellerOrderDetail.ProductDetail> products = orderItems.stream()
                .map(item -> {
                    // 해당 orderItem에 속하는 옵션 리스트 가져오기
                    List<CakeOrderItemOption> itemOptions = optionsByOrderItemId.getOrDefault(item.getOrderItemId(), Collections.emptyList());

                    // SellerOrderDetail.ProductDetail DTO의 options 필드는 Map<String, Integer> 형태
                    // String은 옵션 이름 (OptionItem.name)이어야 합니다.
                    Map<String, Integer> optsMap = itemOptions.stream()
                            .collect(Collectors.toMap(
                                    o -> o.getCakeOptionMapping().getOptionItem().getOptionName(), // 수정: 옵션 이름으로 매핑
                                    CakeOrderItemOption::getOptionCnt
                            ));
                    return SellerOrderDetail.ProductDetail.builder()
                            .name(item.getCakeItem().getCname())
                            .quantity(item.getQuantity())
                            .unitPrice(item.getUnitPrice())
                            .subTotalPrice(item.getSubTotalPrice())
                            .thumbnailImageUrl(item.getCakeItem().getThumbnailImageUrl())
                            .options(optsMap) // 올바르게 조합된 옵션 Map 설정
                            .build();
                })
                .collect(Collectors.toList());

        // 7. BuyerInfo DTO 생성 (Member 정보는 이미 FETCH JOIN으로 가져왔으므로 추가 쿼리 없음)
        SellerOrderDetail.BuyerInfo buyer = new SellerOrderDetail.BuyerInfo(
                order.getMember().getUname(),
                order.getMember().getPhoneNumber()
        );

        // 8. 최종 SellerOrderDetail.Response DTO 생성 및 반환
        return SellerOrderDetail.Response.builder()
                .orderId(order.getOrderId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus().name())
                .pickupDate(order.getPickupDate())
                .pickupTime(order.getPickupTime())
                .OrderTotalPrice(order.getOrderTotalPrice())
                .orderNote(order.getOrderNote())
                .buyer(buyer)
                .products(products)
                .discountAmount(order.getDiscountAmount())
                .finalPaymentAmount(order.getFinalPaymentAmount())
                .build();
    }


    //특정 가게(shopId)에서 해당 주문(orderId)의 상태를 업데이트
    @Override
    @Transactional
    public void updateOrderStatus(Long shopId, Long orderId, String statusStr) {
        // 1) 주문 조회
        CakeOrder order = sellerOrderRepository
                .findByOrderIdAndShopId(orderId, shopId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ORDER_ID));

        System.out.println("DEBUG: Current Order Status (Before Change): " + order.getStatus()); // 디버그 로그
        System.out.println("DEBUG: Attempting to change to Status: " + statusStr); // 디버그 로그

        // 2) 문자열 → Enum 변환
        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(statusStr);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.INVALID_TYPE);
        }

        // 3) 상태 전환 가능 여부 검증
        boolean valid;
        switch (order.getStatus()) {
            case RESERVATION_PENDING: // 예약 대기 중
                valid = (newStatus == OrderStatus.RESERVATION_CONFIRMED ||
                        newStatus == OrderStatus.RESERVATION_CANCELLED);
                break;
            case RESERVATION_CONFIRMED: // 예약 확정 (픽업 준비/픽업 완료/노쇼 가능)
                valid = (newStatus == OrderStatus.PREPARING || // 추가
                        newStatus == OrderStatus.READY_FOR_PICKUP || // 추가
                        newStatus == OrderStatus.PICKUP_COMPLETED ||
                        newStatus == OrderStatus.NO_SHOW);
                break;
            case PREPARING: // 준비 중
                valid = (newStatus == OrderStatus.READY_FOR_PICKUP ||
                        newStatus == OrderStatus.PICKUP_COMPLETED ||
                        newStatus == OrderStatus.RESERVATION_CANCELLED); // 준비 중에도 취소 가능 여부 (정책에 따라 추가)
                break;
            case READY_FOR_PICKUP: // 픽업 준비 완료
                valid = (newStatus == OrderStatus.PICKUP_COMPLETED ||
                        newStatus == OrderStatus.NO_SHOW); // 픽업 준비 완료 상태에서 취소는 보통 불가능
                break;
            case PICKUP_COMPLETED: // 픽업 완료 (더 이상 변경 불가, 최종 상태)
                valid = false; // 픽업 완료는 최종 상태이므로 더 이상 다른 상태로 변경할 수 없음
                break;
            case RESERVATION_CANCELLED: // 이미 취소됨 (변경 불가, 최종 상태)
                valid = false;
                break;
            case NO_SHOW: // 노쇼 처리됨 (변경 불가, 최종 상태)
                valid = false;
                break;
            default: // 정의되지 않거나 예상치 못한 현재 상태
                valid = false;
        }
        if (!valid) {
            System.out.println("DEBUG: Invalid Status Transition from " + order.getStatus() + " to " + newStatus); // 디버그 로그
            throw new BusinessException(ErrorCode.ORDER_MISMATCH,
                    String.format("현재 주문 상태 (%s) 에서 %s(으)로 변경할 수 없습니다.", order.getStatus(), newStatus));
        }

        order.updateStatus(newStatus);

        sellerOrderRepository.save(order);

        shopScheduleService.adjustScheduleSlotsForOrderStatusChange(order, newStatus);
        temperatureService.updateTemperature(orderId,null);

        //  주문이 '예약 확정' 상태가 되었을 때 구매자 알림
        if (newStatus == OrderStatus.RESERVATION_CONFIRMED) {
            try {
                pickupReminderSchedulingService.schedulePickupReminder(order); // DB에 저장
                System.out.println("DEBUG: RESERVATION_CONFIRMED 상태로 변경되어 픽업 알림 DB 스케줄링 완료: 주문 ID " + order.getOrderId());

                if (order.getMember() != null) {
                    // 픽업 날짜와 시간을 포함한 메시지 생성
                    String pickupDateStr = order.getPickupDate().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"));
                    String pickupTimeStr = order.getPickupTime().format(DateTimeFormatter.ofPattern("HH시 mm분"));
                    String messageTitle = String.format("예약이 확정되었습니다. 픽업 일시: %s %s", pickupDateStr, pickupTimeStr);

                    notificationService.sendNotification(
                            order.getMember().getUid(),
                            messageTitle, // 픽업 날짜와 시간이 포함된 메시지
                            order.getOrderId(),
                            NotificationType.RESERVATION_CONFIRMATION
                    );

                    System.out.println("DEBUG: RESERVATION_CONFIRMED 상태로 변경되어 구매자에게 즉시 알림 전송 완료: 주문 ID " + order.getOrderId());
                } else {
                    System.err.println("DEBUG: 주문 ID " + order.getOrderId() + "에 연결된 멤버(구매자) 정보가 NULL입니다. 알림을 보낼 수 없습니다.");
                }
            } catch (Exception e) {
                System.err.println("DEBUG: 주문 확정 후 픽업 알림 DB 스케줄링 실패: 주문 ID " + order.getOrderId() + ", 에러: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // 주문이 "예약 취소" 상태가 되었을 때 구매자 알림
        if (newStatus == OrderStatus.RESERVATION_CANCELLED) {
            try {
                if (order.getMember() != null) {
                    String messageContent = String.format("주문이 취소되었습니다. 주문 번호 %s", order.getOrderNumber());
                    notificationService.sendNotification(
                            order.getMember().getUid(),
                            messageContent,
                            order.getOrderId(),
                            NotificationType.CANCELLED_ORDER
                    );
                    System.out.println("DEBUG: 주문이 취소되어 구매자에게 알림 전송 완료: 주문 ID " + order.getOrderId());
                } else {
                    System.err.println("DEBUG: 주문 ID " + order.getOrderId() + "에 연결된 멤버(구매자) 정보가 NULL입니다. 취소 알림을 보낼 수 없습니다.");
                }
            } catch (Exception e) {
                System.err.println("DEBUG: 주문 취소 후 알림 전송 실패: 주문 ID " + order.getOrderId() + ", 에러: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // 픽업 준비 완료 상태일 때 구매자 알림
        if (newStatus == OrderStatus.READY_FOR_PICKUP) {
            try {
                if (order.getMember() != null) {

                    String messageContent = "주문하신 케이크 픽업 준비 완료되었습니다! " + order.getShop().getShopName() + "에서 픽업 가능합니다.";

                    notificationService.sendNotification(
                            order.getMember().getUid(), // 구매자 UID
                            messageContent,
                            order.getOrderId(),
                            NotificationType.READY_FOR_PICKUP // 새로 추가된 알림 타입
                    );
                    System.out.println("DEBUG: READY_FOR_PICKUP 상태로 변경되어 구매자에게 픽업 준비 완료 알림 전송 완료: 주문 ID " + order.getOrderId());

                } else {
                    System.err.println("DEBUG: 주문 ID " + order.getOrderId() + "에 연결된 멤버(구매자) 정보가 NULL입니다. 픽업 준비 완료 알림을 보낼 수 없습니다.");
                }
            } catch (Exception e) {
                System.err.println("DEBUG: 픽업 준비 완료 알림 전송 실패: 주문 ID " + order.getOrderId() + ", 에러: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // 노쇼 상태일 때 구매자 알림
        if (newStatus == OrderStatus.NO_SHOW) {
            try {
                if (order.getMember() != null) {
                    String pickupDateStr = order.getPickupDate().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"));
                    String pickupTimeStr = order.getPickupTime().format(DateTimeFormatter.ofPattern("HH시 mm분"));

                    String messageContent = String.format("%s %s 픽업예정이었던 케이크가 픽업되지 않아 노쇼 처리되었습니다.", pickupDateStr, pickupTimeStr);

                    notificationService.sendNotification(
                            order.getMember().getUid(), // 구매자 UID
                            messageContent,
                            order.getOrderId(),
                            NotificationType.NO_SHOW_CONFIRMATION
                    );
                    System.out.println("DEBUG: NO_SHOW 상태로 변경되어 구매자에게 노쇼 처리 알림 전송 완료: 주문 ID " + order.getOrderId());
                } else {
                    System.err.println("DEBUG: 주문 ID " + order.getOrderId() + "에 연결된 멤버(구매자) 정보가 NULL입니다. 노쇼 처리 알림을 보낼 수 없습니다.");
                }
            } catch (Exception e) {
                System.err.println("DEBUG: 노쇼 처리 알림 전송 실패: 주문 ID " + order.getOrderId() + ", 에러: " + e.getMessage());
                e.printStackTrace();
            }
        }

        if (newStatus == OrderStatus.PICKUP_COMPLETED) {
            // (1) Temperature 엔티티에서 grade 조회
            var tempOpt = temperatureRepository.findByMember(order.getMember());
            Grade grade = tempOpt
                    .map(Temperature::getGrade)
                    .orElse(Grade.BASIC); // 없으면 BASIC으로 간주

            // (2) 등급별 적립율 결정
            double rate = getEarnRateByGrade(grade);

            // (3) 계산된 적립 포인트
            long earnedPoints = Math.round(order.getOrderTotalPrice() * rate);

            if (earnedPoints > 0) {
                pointService.changePoint(
                        order.getMember().getUid(),
                        earnedPoints,
                        String.format("구매 적립(%s 등급 %s%%)",
                                grade.name(),
                                rate * 100)
                );
            }
        }

        // 뱃지 부여
        if (order.getMember() != null) {
            badgeService.checkAndAcquireBadges(order.getMember().getUid());
            System.out.println("DEBUG: 주문 상태 변경(" + newStatus + ") 후 회원 UID " + order.getMember().getUid() + "의 모든 뱃지 조건 검사 및 부여/갱신 완료.");
        } else {
            System.err.println("DEBUG: 주문 ID " + order.getOrderId() + "에 연결된 멤버(구매자) 정보가 NULL입니다. 뱃지를 확인할 수 없습니다.");
        }

        System.out.println("DEBUG: Order Status Successfully Updated to: " + order.getStatus()); // 디버그 로그
    }


    @Override
    public SellerStatistics.Response getSellerStatistics(Long shopId, LocalDate startDate, LocalDate endDate) {
        // 1. 날짜 범위 변환 (LocalDate를 LocalDateTime으로 변환, `regDate` 기준)
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atStartOfDay().plusDays(1).minusNanos(1);

        // 2. 통계 지표 계산 (shopId 및 regDate 필터링 적용)

        // 2.1. 주문 건수 통계
        Long orderTotalCount = sellerOrderRepository.countByShopShopIdAndRegDateBetween(shopId, startDateTime, endDateTime);

        Long completedOrderCount = sellerOrderRepository.countByShopShopIdAndRegDateBetweenAndStatus(shopId, startDateTime, endDateTime, OrderStatus.PICKUP_COMPLETED);

        // 취소된 주문 상태들
        List<OrderStatus> cancelledStatuses = Arrays.asList(
                OrderStatus.RESERVATION_CANCELLED
        );
        Long cancelledOrderCount = sellerOrderRepository.countByShopShopIdAndRegDateBetweenAndStatusIn(shopId, startDateTime, endDateTime, cancelledStatuses);

        // 진행 중인 주문 상태들
        List<OrderStatus> inProgressStatuses = Arrays.asList(
                OrderStatus.RESERVATION_PENDING,
                OrderStatus.RESERVATION_CONFIRMED,
                OrderStatus.PREPARING,
                OrderStatus.READY_FOR_PICKUP
        );
        Long inProgressOrderCount = sellerOrderRepository.countByShopShopIdAndRegDateBetweenAndStatusIn(shopId, startDateTime, endDateTime, inProgressStatuses);


        // 2.2. 총 판매 금액 (완료된 주문 기준)
        Double rawTotalSalesAmount = sellerOrderRepository.sumOrderTotalPriceByShopShopIdAndRegDateBetweenAndStatus(shopId, startDateTime, endDateTime, OrderStatus.PICKUP_COMPLETED);
        Long totalSalesAmount = (rawTotalSalesAmount != null) ? rawTotalSalesAmount.longValue() : 0L;


        // 2.3. 평균 주문 금액 (총 판매 금액 / 완료된 주문 건수)
        Double averageSalesAmount = 0.0;
        if (completedOrderCount != null && completedOrderCount > 0) { // 완료된 주문 건수로 평균 계산
            averageSalesAmount = (double) totalSalesAmount / completedOrderCount;
        }

        // 2.4. 주문 상태별 건수
        Map<String, Long> orderStatusCounts = new HashMap<>();
        sellerOrderRepository.countOrderStatusByShopShopIdAndRegDateBetween(shopId, startDateTime, endDateTime)
                .forEach(result -> orderStatusCounts.put(result[0].toString(), (Long) result[1]));


        // 2.5. 상위 판매 상품 랭킹 (topSellingProducts 필드용 - 썸네일 포함)
        List<Object[]> topSellingProductsRaw = sellerOrderRepository.findTopSellingProductsByShopIdAndRegDateBetween(shopId, startDateTime, endDateTime);

        List<SellerStatistics.Response.ProductSalesRanking> topSellingProducts = topSellingProductsRaw.stream()
                .map(result -> {
                    // 쿼리: SELECT ci.cake_id, ci.cname, SUM(coi.quantity), SUM(coi.sub_total_price), ci.thumbnail_image_url
                    return SellerStatistics.Response.ProductSalesRanking.builder()
                            .cakeId((Long) result[0])
                            .cname((String) result[1])
                            .totalQuantity((Long) result[2])
                            .totalSaleAmount((Long) result[3])
                            .thumbnailImageUrl((String) result[4])
                            .build();
                })
                .collect(Collectors.toList());

        // --- 새로운 필드 계산 (총 판매량 화면용) ---

        // 2.1. totalQuantityOverall (총 판매량: 선택 기간 내 완료된 아이템 총 수량)
        Long totalQuantityOverall = cakeOrderItemRepository.sumTotalQuantityByShopIdAndRegDateBetweenAndStatusCompleted(shopId, startDateTime, endDateTime);
        if (totalQuantityOverall == null) totalQuantityOverall = 0L;

        // 2.2. 월별 비교 판매량 (currentMonthQuantity, previousMonthQuantity)
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfCurrentMonth = today.withDayOfMonth(1);
        LocalDate lastDayOfCurrentMonth = today.withDayOfMonth(today.lengthOfMonth());

        LocalDate firstDayOfPreviousMonth = firstDayOfCurrentMonth.minusMonths(1);
        LocalDate lastDayOfPreviousMonth = lastDayOfCurrentMonth.minusMonths(1);

        LocalDateTime currentMonthStart = firstDayOfCurrentMonth.atStartOfDay();
        LocalDateTime currentMonthEnd = lastDayOfCurrentMonth.atTime(LocalTime.MAX);

        LocalDateTime previousMonthStart = firstDayOfPreviousMonth.atStartOfDay();
        LocalDateTime previousMonthEnd = lastDayOfPreviousMonth.atTime(LocalTime.MAX);

        Long currentMonthQuantity = cakeOrderItemRepository.sumTotalQuantityByShopIdAndRegDateBetweenAndStatusCompleted(shopId, currentMonthStart, currentMonthEnd);
        if (currentMonthQuantity == null) currentMonthQuantity = 0L;

        Long previousMonthQuantity = cakeOrderItemRepository.sumTotalQuantityByShopIdAndRegDateBetweenAndStatusCompleted(shopId, previousMonthStart, previousMonthEnd);
        if (previousMonthQuantity == null) previousMonthQuantity = 0L;


        // 2.3. monthlySalesTrend (6개월 차트 데이터)
        List<SellerStatistics.Response.MonthlySalesData> monthlySalesTrend = new ArrayList<>();
        List<Object[]> rawMonthlyData = cakeOrderItemRepository.findMonthlySalesTrendByShopIdAndRegDateBetweenAndStatusCompleted(shopId, startDateTime, endDateTime);
        for (Object[] row : rawMonthlyData) {
            monthlySalesTrend.add(SellerStatistics.Response.MonthlySalesData.builder()
                    .monthYear((String) row[0])
                    .totalQuantity((Long) row[1])
                    .totalSales((Long) row[2])
                    .build());
        }


        // 2.4. productSalesTable (상품별 판매 테이블 데이터)
        List<SellerStatistics.Response.ProductSalesTableItem> productSalesTable = new ArrayList<>();
        List<Object[]> rawProductTableData = cakeOrderItemRepository.findProductSalesTableByShopIdAndRegDateBetweenAndStatusCompleted(shopId, startDateTime, endDateTime);
        for (Object[] row : rawProductTableData) {
            productSalesTable.add(SellerStatistics.Response.ProductSalesTableItem.builder()
                    .cakeId((Long) row[0])
                    .cname((String) row[1])
                    .totalQuantity((Long) row[2])
                    .totalSaleAmount((Long) row[3])
                    .build());
        }


        // 2.5. lowestRankingProducts (아쉬운 랭킹)
        List<SellerStatistics.Response.ProductRankingItem> lowestRankingProducts = new ArrayList<>();
        List<Object[]> rawLowestRanking = cakeOrderItemRepository.findLowest3RankingProductsNative(shopId, startDateTime, endDateTime);
        for (Object[] row : rawLowestRanking) {
            lowestRankingProducts.add(SellerStatistics.Response.ProductRankingItem.builder()
                    .cakeId((Long) row[0])
                    .cname((String) row[1])
                    .build());
        }

        // topRankingProducts ( 인기 순위)
        List<SellerStatistics.Response.ProductRankingItem> topRankingProductsForBuilder = new ArrayList<>(); // 변수명 변경 (중복 피함)
        List<Object[]> rawTopRankingForBuilder = cakeOrderItemRepository.findTop3RankingProductsNative(shopId, startDateTime, endDateTime);
        for (Object[] row : rawTopRankingForBuilder) {
            topRankingProductsForBuilder.add(SellerStatistics.Response.ProductRankingItem.builder()
                    .cakeId((Long) row[0])
                    .cname((String) row[1])
                    .build());
        }


        // 3. 최종 응답 DTO 빌드 및 반환
        return SellerStatistics.Response.builder()
                // 기존 필드
                .orderTotalCount(orderTotalCount)
                .completedOrderCount(completedOrderCount)
                .cancelledOrderCount(cancelledOrderCount)
                .inProgressOrderCount(inProgressOrderCount)
                .totalSalesAmount(totalSalesAmount)
                .averageSalesAmount(averageSalesAmount)
                .orderStatusCounts(orderStatusCounts)
                .topSellingProducts(topSellingProducts) // 기존 필드 (thumbnailImageUrl 포함)

                // 새로 추가된 필드들 (총 판매량 화면용)
                .totalQuantityOverall(totalQuantityOverall)
                .currentMonthQuantity(currentMonthQuantity)
                .previousMonthQuantity(previousMonthQuantity)
                .monthlySalesTrend(monthlySalesTrend)
                .productSalesTable(productSalesTable)
                .lowestRankingProducts(lowestRankingProducts)
                .topRankingProducts(topRankingProductsForBuilder)

                // 조회 기간 및 생성 시각
                .startDate(startDate)
                .endDate(endDate)
                .generatedAt(LocalDateTime.now())
                .build();
    }
    @Override
    public byte[] getSellerStatisticsPdf(Long shopId, LocalDate startDate, LocalDate endDate) {
        // 1. 먼저 통계 데이터를 가져옵니다.
        SellerStatistics.Response statistics = getSellerStatistics(shopId, startDate, endDate);

        // 2. PDF 문서 생성 시작
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PDPage page = new PDPage();
            document.addPage(page);

            // 폰트 로드 (한글 지원을 위해 외부 폰트 필요)
            PDType0Font font = PDType0Font.load(document, new ClassPathResource("fonts/NanumGothic.ttf").getInputStream());

            // ⭐ NumberFormat 인스턴스 생성 (천 단위 콤마를 위해) ⭐
            NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.KOREA); // 한국 지역 설정으로 천 단위 콤마 포맷팅

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.setFont(font, 12);
                contentStream.beginText();
                contentStream.setLeading(14.5f); // 줄 간격

                contentStream.newLineAtOffset(50, 750); // 시작 위치

                contentStream.showText("판매자 통계 보고서");
                contentStream.newLine();
                contentStream.showText("조회 기간: " + startDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd")) + " ~ " + endDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd")));
                contentStream.newLine();
                contentStream.newLine();

                contentStream.showText("총 주문 건수: " + statistics.getOrderTotalCount());
                contentStream.newLine();
                contentStream.showText("완료된 주문 건수: " + statistics.getCompletedOrderCount());
                contentStream.newLine();
                contentStream.showText("취소된 주문 건수: " + statistics.getCancelledOrderCount());
                contentStream.newLine();
                contentStream.showText("진행 중인 주문 건수: " + statistics.getInProgressOrderCount());
                contentStream.newLine();
                contentStream.newLine();

                // ⭐ 오류 발생 부분 수정 ⭐
                // statistics.getTotalSalesAmount().toLocaleString() 대신 numberFormat.format() 사용
                contentStream.showText("총 판매 금액 (완료된 주문 기준): " + numberFormat.format(statistics.getTotalSalesAmount()) + "원");
                contentStream.newLine();
                contentStream.showText("평균 주문 금액: " + String.format("%.2f", statistics.getAverageSalesAmount()) + "원");
                contentStream.newLine();
                contentStream.newLine();

                contentStream.showText("---- 주문 상태별 건수 ----");
                contentStream.newLine();
                statistics.getOrderStatusCounts().forEach((status, count) -> {
                    try {
                        String statusKr = OrderStatus.valueOf(status).getKr();
                        contentStream.showText("- " + statusKr + ": " + count + "건");
                        contentStream.newLine();
                    } catch (IOException e) {
                        throw new RuntimeException("PDF content error: " + e.getMessage(), e);
                    } catch (IllegalArgumentException e) {
                        throw new RuntimeException("Unknown OrderStatus: " + status, e);
                    }
                });
                contentStream.newLine();

                // ... (더 많은 통계 데이터를 PDF에 추가)

                contentStream.endText();
            }
            document.save(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "PDF 보고서 생성 중 IO 오류 발생: " + e.getMessage());
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "PDF 보고서 생성 중 예기치 못한 오류 발생: " + e.getMessage());
        }
    }

    private double getEarnRateByGrade(Grade grade) {
        switch (grade) {
            case VIP:  return 0.01;   // 1%
            case VVIP: return 0.015;  // 1.5%
            default:   return 0.0;    // BASIC/FROZEN 은 적립 없음
        }
    }

}
