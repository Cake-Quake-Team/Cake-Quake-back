package com.cakequake.cakequakeback.temperature.service;

import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.member.repo.MemberRepository;
import com.cakequake.cakequakeback.order.entities.CakeOrder;
import com.cakequake.cakequakeback.order.entities.OrderStatus;
import com.cakequake.cakequakeback.order.repo.BuyerOrderRepository;
import com.cakequake.cakequakeback.order.repo.CakeOrderItemRepository;
import com.cakequake.cakequakeback.point.dto.PointHistoryResponseDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    @Override
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
    @Override
    public InfiniteScrollResponseDTO<TemperatureHistoryResponseDTO> findHistory(PageRequestDTO pageRequestDTO, Long uid) {

        Member member = memberRepository.findById(uid)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 사용자입니다."));

        Pageable pageable = pageRequestDTO.getPageable("regDate");

        // 리포지토리의 @Query가 이미 DTO로 프로젝션하므로, 바로 DTO Page를 받습니다.
        Page<TemperatureHistoryResponseDTO> dtoPage = temperatureHistoryRepository.findByMember(member, pageable);

        // InfiniteScrollResponseDTO의 제네릭 타입도 올바르게 <TemperatureHistoryResponseDTO>로 지정해야 합니다.
        return InfiniteScrollResponseDTO.<TemperatureHistoryResponseDTO>builder()
                .content(dtoPage.getContent())
                .hasNext(dtoPage.hasNext())
                .totalCount((int) dtoPage.getTotalElements())
                .build();
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
                ChangeReason.PICKUP_COMPLETED,
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
        Member member = review.getMember();  // Review에 getMember() 있어야 함
        log.info("리뷰 작성자: uid = {}", member.getUserId());

        // 3. 온도 정보 조회
        Temperature temperature = temperatureRepository.findByMember(member).get();
        String picture = review.getReviewPictureUrl();
        float changeAmount;

        if (picture != null && !picture.isBlank()) {
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

    //회원가입시 초기 온도 데이터 생성
    public Temperature createInitialTemperature(Member member) {
        log.info("초기 Temperature 생성 시작합니다. uid = {}", member.getUid());

        try {
            Temperature initialTemperature = Temperature.builder()
                    .member(member)
                    .temperature(36.5) // 초기 매너 온도
                    .grade(Grade.fromTemperature(36.5)) // 초기 온도에 따른 등급 설정
                    .changeAmount(0.0f) // 최초 생성 시 변화량 0
                    .reason(ChangeReason.ADMIN_ADJUSTMENT) // 초기 생성임을 명시
                    .relatedObjectType(RelatedObjectType.SYSTEM) // 멤버 생성으로 인한 변화
                    .relatedObjectId(member.getUid().toString()) // 관련 객체 ID는 멤버의 UID
                    .build();

            log.debug("Member UID {} 를 위한 Temperature 객체 생성 완료: {}", member.getUid(), initialTemperature);

            Temperature savedTemperature = temperatureRepository.save(initialTemperature);
            log.info("Member UID {} 에 대한 초기 Temperature 데이터가 성공적으로 저장되었습니다. Temperature UID: {}", member.getUid(), savedTemperature.getUid());
            return savedTemperature;

        } catch (Exception e) {
            log.error("Member UID {} 에 대한 초기 Temperature 생성 중 오류 발생: {}", member.getUid(), e.getMessage(), e);
            throw e; // 트랜잭션 롤백을 위해 예외를 다시 던짐
        }
    }

}






