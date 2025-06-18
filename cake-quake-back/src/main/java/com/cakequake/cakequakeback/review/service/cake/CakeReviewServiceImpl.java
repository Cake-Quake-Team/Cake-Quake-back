package com.cakequake.cakequakeback.review.service.cake;

import com.cakequake.cakequakeback.cake.item.repo.CakeItemRepository;
import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.review.dto.ReviewResponseDTO;;
import com.cakequake.cakequakeback.review.repo.cake.CakeReviewRepo;
import com.cakequake.cakequakeback.review.validator.CakeReviewValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Log4j2
public class CakeReviewServiceImpl implements CakeReviewService{

    private final CakeReviewRepo cakeReviewRepo;
    private final CakeItemRepository cakeItemRepository;
    private final CakeReviewValidator validator;

    //케이크(단품) 리뷰 전체 조회
    @Override
    public InfiniteScrollResponseDTO<ReviewResponseDTO> getCakeItemReviews(Long cakeItemId, PageRequestDTO pageRequestDTO) {
        // (0) 케이크 존재 여부 검증
        validator.validateCakeExists(cakeItemId);

        Pageable pageable = pageRequestDTO.getPageable("regDate");

        Page<ReviewResponseDTO> page = cakeReviewRepo.listOfCakeReviews(cakeItemId,pageable);

        return InfiniteScrollResponseDTO.<ReviewResponseDTO>builder()
                .content(page.getContent())
                .hasNext(page.hasNext())
                .totalCount((int) page.getTotalElements())
                .build();
    }


    //케이크 단품 리뷰 상세 보기
    @Override
    public ReviewResponseDTO getReview(Long reviewId,Long cakeItemId) {
        // 존재 + 소속 검증 후 DTO 반환
        return validator.validateReviewBelongsToCake(reviewId, cakeItemId);
    }
}
