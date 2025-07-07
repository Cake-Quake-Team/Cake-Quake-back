package com.cakequake.cakequakeback.review.service.buyer;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.cakequake.cakequakeback.badge.constants.BadgeConstants;
import com.cakequake.cakequakeback.badge.service.BadgeService;
import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.common.utils.CustomImageUtils;
import com.cakequake.cakequakeback.order.entities.CakeOrder;
import com.cakequake.cakequakeback.order.entities.CakeOrderItem;
import com.cakequake.cakequakeback.order.repo.BuyerOrderRepository;
import com.cakequake.cakequakeback.order.repo.CakeOrderItemRepository;
import com.cakequake.cakequakeback.point.service.PointService;
import com.cakequake.cakequakeback.review.dto.ReviewRequestDTO;
import com.cakequake.cakequakeback.review.dto.ReviewResponseDTO;
import com.cakequake.cakequakeback.review.entities.Review;
import com.cakequake.cakequakeback.review.event.ReviewChangedEvent;
import com.cakequake.cakequakeback.review.repo.buyer.BuyerReviewRepo;
import com.cakequake.cakequakeback.review.validator.BuyerReviewValidator;
import com.cakequake.cakequakeback.temperature.service.TemperatureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;


@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
public class BuyerReviewServiceImpl implements BuyerReviewService {

    private final AmazonS3 amazonS3;
    private final String bucketName = "elasticbeanstalk-ap-northeast-2-853972008946";
    private final String UPLOAD_DIR = "images/reviewImages/";

    private final BuyerReviewRepo buyerReviewRepo;
    private final BuyerOrderRepository buyerOrderRepo;
    private final CakeOrderItemRepository cakeOrderItemRepository;
    private final PointService pointService;
    private final CustomImageUtils imageUtils;
    private final BuyerReviewValidator validator;

    private final ApplicationEventPublisher eventPublisher;

    private final TemperatureService temperatureService;
    private final BadgeService badgeService;


    //구매자 리뷰 추가
    @Override
    public ReviewResponseDTO createReview(Long orderId, ReviewRequestDTO dto, Long uid) {

        // 주문·권한 검증
        CakeOrder order = validator.validateOrderBelongsToUser(orderId, uid);
        // 중복 리뷰 방지
        validator.validateNotReviewedYet(orderId, dto.getCakeId());
        // 주문 아이템 검증
        CakeOrderItem item = validator.validateOrderItemExists(orderId, dto.getCakeId());

        // 이미지 파일 저장
        MultipartFile file = dto.getReviewPictureUrl();
        String pictureUrl = null;
        if (file != null && !file.isEmpty()) {
            try {
                String savedName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                String key = UPLOAD_DIR + savedName;

                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentLength(file.getSize());

                amazonS3.putObject(bucketName, key, file.getInputStream(), metadata);

                pictureUrl = amazonS3.getUrl(bucketName, key).toString();
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.IMAGE_GENERATION_FAILED);
            }
        }

        // Review 엔티티 생성
        Review review = Review.builder()
                .order(order)
                .member(order.getMember())
                .shop(order.getShop())
                .cakeItem(item.getCakeItem())
                .rating(dto.getRating())
                .content(dto.getContent())
                .reviewPictureUrl(pictureUrl)
                .build();

        // 엔티티 저장
        Review savedReview = buyerReviewRepo.save(review);

        Long reviewerUid = order.getMember().getUid();
        long amount = (pictureUrl != null) ? 1000L : 500L;
        String desc  = (pictureUrl != null)
                ? "사진 리뷰 작성 보상"
                : "텍스트 리뷰 작성 보상";
        pointService.changePoint(reviewerUid, amount, desc);

        // 뱃지 부여
        badgeService.acquireBadge(reviewerUid, BadgeConstants.REVIEW_STARTER_BADGE_ID); // '리뷰 스타터' 뱃지 부여
        badgeService.acquireBadge(reviewerUid, BadgeConstants.REVIEW_MASTER_BADGE_ID);  // '리뷰 마스터' 뱃지 부여

        // **리뷰 변경 이벤트 발행**
        log.info("[DEBUG] ReviewChangedEvent 발행 → shopId={}", savedReview.getShop().getShopId());
        eventPublisher.publishEvent(
                new ReviewChangedEvent(this, savedReview.getShop().getShopId())
        );



        // 프로젝션(selectDTO)으로 바로 DTO 반환
        ReviewResponseDTO response = buyerReviewRepo.selectDTO(savedReview.getReviewId());
        temperatureService.updateTemperature(orderId, savedReview.getReviewId());

        if(response == null){
            throw new IllegalStateException("DTO 조회 실패");
        }
        return response;
    }

    //구매자 전체 리뷰 조회
    @Override
    @Transactional(readOnly = true)
    public InfiniteScrollResponseDTO<ReviewResponseDTO> getMyReviews(PageRequestDTO pageRequestDTO, Long userId) {

        //pageRequestDTO로 부터 Pageable생성 (regDate 내림차순)
        Pageable pageable = pageRequestDTO.getPageable("regDate");

        Page<ReviewResponseDTO> page = buyerReviewRepo.listOfUserReviews(userId, pageable);

        //DTO로 변환
        return InfiniteScrollResponseDTO.<ReviewResponseDTO>builder()
                .content(page.getContent())
                .hasNext(page.hasNext())
                .totalCount((int) page.getTotalElements() )
                .build();
    }

    //구매자 리뷰 단건 조회
    @Override
    @Transactional(readOnly = true)
    public ReviewResponseDTO getReview(Long reviewId, Long uid) {
         // 존재 여부 및 소유권 검증 (예외 발생 시 404 또는 권한 에러)
        Review review = validator.validateReviewOwnership(reviewId, uid);
        // 2) 삭제 상태 검증
        validator.validateNotDeleted(review);

        return buyerReviewRepo.selectDTO(reviewId);

    }
    //구매자가 자신 리뷰 수정
    @Override
    public ReviewResponseDTO updateReview(Long reviewId, ReviewRequestDTO dto, Long uid) {
        // 존재 여부 및 소유권 검증
        Review review = validator.validateReviewOwnership(reviewId, uid);
        validator.validateNotDeleted(review);

        // 3) 새 파일이 업로드 되었으면 저장하고 URL 갱신
        MultipartFile file = dto.getReviewPictureUrl();
        if (file != null && !file.isEmpty()) {
            try {
                String savedName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                String key = UPLOAD_DIR + savedName;

                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentLength(file.getSize());

                amazonS3.putObject(bucketName, key, file.getInputStream(), metadata);

                String pictureUrl = amazonS3.getUrl(bucketName, key).toString();
                review.updateReviewPictureUrl(pictureUrl);
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.IMAGE_GENERATION_FAILED);
            }
        }

        //수정 가능한 필드만 수정하기
        review.updateRating(dto.getRating());
        review.updateContent(dto.getContent());

        buyerReviewRepo.save(review);


        // **리뷰 변경 이벤트 발행**
        eventPublisher.publishEvent(
                new ReviewChangedEvent(this, review.getShop().getShopId())
        );

        return buyerReviewRepo.selectDTO(reviewId);
    }

    //리뷰 삭제
    @Override
    public void deleteReview(Long reviewId, Long uid) {
        // 존재 여부 및 소유권 검증
        Review review = validator.validateReviewOwnership(reviewId, uid);
        validator.validateNotDeleted(review);

        //삭제 -> 상태만 DELETE로 변경
        review.deleteByBuyer();

        // **리뷰 변경 이벤트 발행**
        eventPublisher.publishEvent(
                new ReviewChangedEvent(this, review.getShop().getShopId())
        );
    }
}
