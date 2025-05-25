package com.cakequake.cakequakeback.review.service.cake;

import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.review.dto.ReviewResponseDTO;
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

    private CakeReviewRepo cakeReviewRepo;


    public CakeReviewServiceImpl(CakeReviewRepo cakeReviewRepo) {
        this.cakeReviewRepo = cakeReviewRepo;
    }


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
}
