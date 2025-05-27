package com.cakequake.cakequakeback.review.cake;

import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.review.dto.ReviewResponseDTO;
import com.cakequake.cakequakeback.review.repo.cake.CakeReviewRepo;
import com.cakequake.cakequakeback.review.service.cake.CakeReviewServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class CakeReviewTest {
    @Mock
    private CakeReviewRepo cakeReviewRepo;

    @InjectMocks
    private CakeReviewServiceImpl cakeReviewService;


    @Test
    @DisplayName("getCakeItemReviews: 페이지당 2개 요청 시 hsaNext= true, totalCount =3")
    void getCakeItemReviews(){
        Long cakeItemId = 1L;
        PageRequestDTO pageRequestDTO = PageRequestDTO.builder()
                .page(1)
                .size(2)
                .build();

        List<ReviewResponseDTO> dtos = List.of(
                new ReviewResponseDTO(1L, 10L, cakeItemId, "초코케익", 5, "굿!", null, LocalDateTime.now(), null, null),
                new ReviewResponseDTO(2L, 11L, cakeItemId, "초코케익", 4, "맛있어요", null, LocalDateTime.now(), null, null)
        );
        Page<ReviewResponseDTO>page = new PageImpl<>(
                dtos,
                pageRequestDTO.getPageable("regDate"),
                3
        );

        given(cakeReviewRepo.listOfCakeReviews(cakeItemId,pageRequestDTO.getPageable("regDate"))).willReturn(page);

        InfiniteScrollResponseDTO<ReviewResponseDTO> result = cakeReviewService.getCakeItemReviews(cakeItemId,pageRequestDTO);

        assertThat(result.getContent()).isEqualTo(dtos);
        assertThat(result.isHasNext()).isTrue();
        assertThat(result.getTotalCount()).isEqualTo(3);
    }


}
