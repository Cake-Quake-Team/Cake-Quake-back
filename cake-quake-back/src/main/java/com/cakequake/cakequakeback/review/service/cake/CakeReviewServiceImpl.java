package com.cakequake.cakequakeback.review.service.cake;

import com.cakequake.cakequakeback.cake.item.repo.CakeItemRepository;
import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.review.dto.ReviewResponseDTO;
import com.cakequake.cakequakeback.review.entities.Review;
import com.cakequake.cakequakeback.review.repo.cake.CakeReviewRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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


    //케이크(단품) 리뷰 전체 조회
    @Override
    public InfiniteScrollResponseDTO<ReviewResponseDTO> getCakeItemReviews(Long cakeItemId, PageRequestDTO pageRequestDTO) {
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
        //케이크 존재 여부
        cakeItemRepository.findById(cakeItemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PRODUCT_ID));

        ReviewResponseDTO dto = cakeReviewRepo.selectDTO(reviewId);
        if (dto == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_REVIEW_ID);
        }
        // 3) 조회된 DTO에 포함된 cakeId가 요청받은 cakeItemId와 일치하는지 검증
        if (!dto.getCakeId().equals(cakeItemId)) {
            //이후 에러코드 추가
            throw new BusinessException(ErrorCode.MISSING_CAKE_ITEM_ID);
        }

        return dto;
    }
}
