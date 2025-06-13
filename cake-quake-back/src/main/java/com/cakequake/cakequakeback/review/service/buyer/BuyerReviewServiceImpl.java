package com.cakequake.cakequakeback.review.service.buyer;

import com.cakequake.cakequakeback.cake.item.entities.CakeItem;
import com.cakequake.cakequakeback.cake.item.repo.CakeItemRepository;
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
import com.cakequake.cakequakeback.review.repo.buyer.BuyerReviewRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
public class BuyerReviewServiceImpl implements BuyerReviewService {

    private static final String UPLOAD_DIR = "C:/nginx-1.26.3/html/reviewuploads";

    private final BuyerReviewRepo buyerReviewRepo;
    private final BuyerOrderRepository buyerOrderRepo;
    private final CakeOrderItemRepository cakeOrderItemRepository;
    private final PointService pointService;
    private final CustomImageUtils imageUtils;

    //구매자 리뷰 추가
    @Override
    public ReviewResponseDTO createReview(Long orderId, ReviewRequestDTO dto, Long uid) {

        log.info("--------------review created-------------");
        log.info("🔍 createReview 호출 — orderId={}, uid={}", orderId, uid);
        //해당 주문서가 존재하지 않거나 권한이 없는 경우
        CakeOrder order = buyerOrderRepo.findByOrderIdAndMemberUid(orderId,uid)
        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ORDER_ID));

        log.info("-----------------123123-------------");
        //이미 리뷰 작성 여부 체크
        Long cakeId = dto.getCakeId();
        if(buyerReviewRepo.findByOrderOrderIdAndCakeItemCakeId(orderId,cakeId).isPresent()){
            throw new BusinessException(ErrorCode.ALREADY_REVIEWED_ORDER);
        }
        log.info("-----------------12314325342523-------------");
        List<CakeOrderItem> orderItems = cakeOrderItemRepository.findByCakeOrder_OrderId(orderId);
        if(orderItems.isEmpty()){
            //에러 새로 추가해야함 INVALID_ORDER_ITEM
            throw new BusinessException(ErrorCode.INVALID_ORDER_ID);
        }
        log.info("uploadDir = {}", UPLOAD_DIR);
        // 4) 이미지 파일 저장
        MultipartFile file = dto.getReviewPictureUrl();
        log.info("file is empty? {}", file == null || file.isEmpty());
        String savedName = null;
        if (file != null && !file.isEmpty()) {
            savedName = imageUtils.saveImageFile(file, UPLOAD_DIR);
        }
        String pictureUrl = (savedName != null)
                ? "/reviewuploads/" + savedName
                : null;

        log.info("▶ orderItems 크기 = {}", orderItems.size());
        orderItems.forEach(item ->
                log.info("   - item.getCakeItem().getCakeId() = {}", item.getCakeItem().getCakeId())
        );

        log.info("▶ 요청된 cakeId = {}", cakeId);

        log.info("-----------------zzzzzzzzzzzzzzzzzzzzzz------------");
        CakeOrderItem cakeOrderItem = orderItems.stream()
                .filter(item -> item.getCakeItem().getCakeId().equals(cakeId))
                .findFirst()
                .orElseThrow(()-> new BusinessException(ErrorCode.INVALID_ORDER_ID));

        CakeItem cakeItem = cakeOrderItem.getCakeItem();


        // 5) Review 엔티티 생성
        Review review = Review.builder()
                .order(order)
                .member(order.getMember())
                .shop(order.getShop())
                .cakeItem(cakeItem)
                .rating(dto.getRating())
                .content(dto.getContent())
                .reviewPictureUrl(pictureUrl)
                .build();

        // 6) 엔티티 저장
        Review savedReview = buyerReviewRepo.save(review);
        log.info("Saved review: " + savedReview.getReviewId());


        Long reviewerUid = order.getMember().getUid();
        long amount = (pictureUrl != null) ? 1000L : 500L;
        String desc  = (pictureUrl != null)
                ? "사진 리뷰 작성 보상"
                : "텍스트 리뷰 작성 보상";
        pointService.changePoint(reviewerUid, amount, desc);



        // 7) 프로젝션(selectDTO)으로 바로 DTO 반환
        ReviewResponseDTO response = buyerReviewRepo.selectDTO(savedReview.getReviewId());

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
        //리뷰 존재 여부 체크
        Review review = buyerReviewRepo.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_REVIEW_ID));
        //본인 리뷰인지 검증
        if (!review.getMember().getUid().equals(uid)) {
            throw new BusinessException(ErrorCode.NOT_AUTHORIZED_OTHER);
        }

        return buyerReviewRepo.selectDTO(reviewId);

    }
    //구매자가 자신 리뷰 수정
    @Override
    public ReviewResponseDTO updateReview(Long reviewId, ReviewRequestDTO dto, Long uid) {
        //리뷰 존재 여부 체크
        Review review = buyerReviewRepo.findById(reviewId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.NOT_FOUND_REVIEW_ID)
                );
        //본인 리뷰인지 검증
        if (!review.getMember().getUid().equals(uid)) {
            throw new BusinessException(ErrorCode.NOT_AUTHORIZED_OTHER);
        }

        // 3) 새 파일이 업로드 되었으면 저장하고 URL 갱신
        MultipartFile file = dto.getReviewPictureUrl();
        if (file != null && !file.isEmpty()) {
            String savedName = imageUtils.saveImageFile(file, UPLOAD_DIR);
            review.setReviewPictureUrl("/reviewuploads/" + savedName);
        }

        //수정 가능한 필드만 수정하기
        review.setRating(dto.getRating());
        review.setContent(dto.getContent());

        buyerReviewRepo.save(review);

        return buyerReviewRepo.selectDTO(reviewId);
    }

    //리뷰 삭제
    @Override
    public void deleteReview(Long reviewId, Long uid) {
        //리뷰 존재 여부 체크
        Review review = buyerReviewRepo.findById(reviewId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.NOT_FOUND_REVIEW_ID)
                );
        //본인 리뷰인지 검증
        if (!review.getMember().getUid().equals(uid)) {
            throw new BusinessException(ErrorCode.NOT_AUTHORIZED_OTHER);
        }

        //삭제 -> 상태만 DELETE로 변경
        review.deleteByBuyer();
    }
}
