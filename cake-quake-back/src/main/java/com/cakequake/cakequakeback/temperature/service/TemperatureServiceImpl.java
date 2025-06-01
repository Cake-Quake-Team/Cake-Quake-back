package com.cakequake.cakequakeback.temperature.service;

import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.member.repo.MemberRepository;
import com.cakequake.cakequakeback.order.entities.CakeOrder;
import com.cakequake.cakequakeback.order.entities.OrderStatus;
import com.cakequake.cakequakeback.order.repo.BuyerOrderRepository;
import com.cakequake.cakequakeback.order.repo.CakeOrderItemRepository;
import com.cakequake.cakequakeback.review.entities.Review;
import com.cakequake.cakequakeback.review.repo.common.CommonReviewRepo;
import com.cakequake.cakequakeback.temperature.dto.TemperatureHistoryResponseDTO;
import com.cakequake.cakequakeback.temperature.dto.TemperatureRequestDTO;
import com.cakequake.cakequakeback.temperature.dto.TemperatureResponseDTO;
import com.cakequake.cakequakeback.temperature.entities.ChangeReason;
import com.cakequake.cakequakeback.temperature.entities.Grade;
import com.cakequake.cakequakeback.temperature.entities.RelatedObjectType;
import com.cakequake.cakequakeback.temperature.entities.Temperature;
import com.cakequake.cakequakeback.temperature.repo.TemperatureHistoryRepository;
import com.cakequake.cakequakeback.temperature.repo.TemperatureRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j

//기능 수정 필요
public class TemperatureServiceImpl implements TemperatureService {
    private final TemperatureRepository temperatureRepository;
    private final TemperatureHistoryRepository temperatureHistoryRepository;
    private final MemberRepository memberRepository;
    private final BuyerOrderRepository buyerOrderRepository;
    private final CommonReviewRepo commonReviewRepo;

    //온도 업데이트
    public void updateTemperature(Long orderId, Long reviewId) {
        Optional<CakeOrder> optionalOrder = buyerOrderRepository.findById(orderId);

        if (optionalOrder.isPresent()) {
            CakeOrder order = optionalOrder.get();

            // 노쇼, 취소, 픽업완료 상태에 따른 온도 처리
            OrderStatus status = order.getStatus();
            switch (status) {
                case NO_SHOW:
                    decreaseNoShow(orderId);
                    break;
                case PICKUP_COMPLETED:
                    increasePickup(orderId);
                    break;
                case RESERVATION_CANCELLED:
                    decreaseCancle(orderId);
                    break;
                default:
                    log.info("처리할 상태 아님: 주문 ID = {}, 상태 = {}", orderId, status);
            }
        } else {
            log.warn("주문을 찾을 수 없음: 주문 ID = {}", orderId);
            // 주문이 없으면 상태에 따른 온도 변화는 건너뜀
        }

        // 리뷰가 있을 경우만 리뷰 온도 증가 처리
        if (reviewId != null) {
            increaseReview(reviewId);
        }

    }

    //특정 회원의 온도 이력 조회
    public List<TemperatureHistoryResponseDTO> findHistory(Long uid) {

        Member member = memberRepository.findById(uid)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 사용자입니다."));

        return temperatureHistoryRepository.findByMember(member)
                .stream()
                .map(history -> TemperatureHistoryResponseDTO.builder()
                        // .temperature(history.getTemperature()) // 보통 이건 DTO에 포함 안 시킴
                        .changeAmount(history.getChangeAmount())
                        .afterTemperature(history.getAfterTemperature())
                        .reason(history.getReason())
                        .relatedObjectType(history.getRelatedObjectType())
                        .relatedObjectId(history.getRelatedObjectId())
                        .modDate(history.getModDate())
                        .build())
                .collect(Collectors.toList());
    }

    //노쇼 할 경우 온도 변화량 감소
    public void decreaseNoShow(Long orderId) {
        log.info("노쇼 처리: 주문 ID = {}", orderId);

        CakeOrder order = buyerOrderRepository.findById(orderId).get();

        Member member = order.getMember();
        log.info("노쇼 처리: 회원 uid = {}", member);

        Temperature temperature = temperatureRepository.findByMember(member).get();

        float changeAmount = -10f; //온도 감소 처리(변화량:-10도)

        temperature.updateTemperature(
                changeAmount,
                ChangeReason.NO_SHOW,
                RelatedObjectType.RESERVATION,
                String.valueOf(orderId)

        );

    }

    //취소 할 경우 온도 변화량 감소
    public void decreaseCancle(Long orderId) {
        log.info("노쇼 처리: 주문 ID = {}", orderId);

        CakeOrder order = buyerOrderRepository.findById(orderId).get();

        LocalDate pickUpDate = order.getPickupDate();
        LocalDate today = LocalDate.now();

        Member member = order.getMember();
        log.info("노쇼 처리: 회원 uid = {}", member);

        Temperature temperature = temperatureRepository.findByMember(member).get();

        float changeAmount;

        // 하루 전 ~ 당일 → -10도
        if (!today.isBefore(pickUpDate.minusDays(1)) && !today.isAfter(pickUpDate)) {
            changeAmount = -10f;
        }
        // 3일 전 ~ 2일 전 → -5도
        else if (!today.isBefore(pickUpDate.minusDays(3)) && today.isBefore(pickUpDate.minusDays(1))) {
            changeAmount = -5f;
        }
        // 그 외 → 온도 변화 없음
        else {
            log.info("온도 변화 없음. 주문 ID: {}", orderId);
            return;
        }

        temperature.updateTemperature(
                changeAmount,
                ChangeReason.RESERVATION_CANCELLED,
                RelatedObjectType.RESERVATION,
                String.valueOf(orderId)
        );
    }

    //픽업 완료시 온도 변화량 증가
    public void increasePickup(Long orderId) {
        log.info("노쇼 처리: 주문 ID = {}", orderId);

        CakeOrder order = buyerOrderRepository.findById(orderId).get();

        Member member = order.getMember();
        log.info("노쇼 처리: 회원 uid = {}", member);

        Temperature temperature = temperatureRepository.findByMember(member).get();
        Grade grade = temperature.getGrade(); //등급가져오기

        float changeAmount;

        // 4. 등급에 따라 변화량 계산
        switch (grade) {
            case FROZEN:
                changeAmount = 1.0f;
                break;
            case BASIC:
                changeAmount = 3.0f;
                break;
            case VIP,VVIP:
                changeAmount = 2.0f;
                break;
            default:
                changeAmount = 0.0f;
        }

        temperature.updateTemperature(
                changeAmount,
                ChangeReason.NO_SHOW,
                RelatedObjectType.RESERVATION,
                String.valueOf(orderId)

        );

    }

    //리뷰 작성시 온도 변화량 증가
    public void increaseReview(Long ReviewId) {

        //1. 리뷰 조회
        Review review = commonReviewRepo.findById(ReviewId).get();
        log.info("노쇼 처리: 리뷰 ID = {}", ReviewId);

        // 2. 리뷰 작성자 정보 조회
        Member member = review.getUser();  // Review에 getMember() 있어야 함
        log.info("리뷰 작성자: uid = {}", member.getUserId());

        // 3. 온도 정보 조회
        Temperature temperature = temperatureRepository.findByMember(member).get();
        String picture = review.getReviewPictureUrl();
        float changeAmount;

        if (!picture.isBlank()) {
            changeAmount = 1.0f;
        } else {
            return;
        }

        // 5. 온도 업데이트
        temperature.updateTemperature(
                changeAmount,
                ChangeReason.REVIEW_WRITTEN,
                RelatedObjectType.REVIEW,
                String.valueOf(ReviewId)
        );

    }

    // uid 이용 온도 조회
    public Temperature getTemperatureByUid(Long uid) {
        return temperatureRepository.findById(uid)
                .orElseThrow(() -> new EntityNotFoundException("해당 uid의 온도 정보가 없습니다."));
    }

    // uid 이용 온도 업데이트 예시
    public void updateByuid(TemperatureRequestDTO request) {
        Member member = memberRepository.findById(request.getUid())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다."));

        Temperature temperature = temperatureRepository.findByMember(member)
                .orElseThrow(() -> new EntityNotFoundException("해당 회원의 온도 정보가 없습니다."));

        temperature.updateTemperature(
                request.getChange(),
                request.getReason(),
                request.getType(),
                request.getRelatedObjectId()
        );

        temperatureRepository.save(temperature);
    }

}




