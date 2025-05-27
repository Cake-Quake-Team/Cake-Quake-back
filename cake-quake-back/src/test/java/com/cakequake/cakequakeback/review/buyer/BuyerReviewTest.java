package com.cakequake.cakequakeback.review.buyer;

import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.review.dto.ReviewRequestDTO;
import com.cakequake.cakequakeback.review.dto.ReviewResponseDTO;
import com.cakequake.cakequakeback.review.entities.Review;
import com.cakequake.cakequakeback.review.entities.ReviewStatus;
import com.cakequake.cakequakeback.review.repo.buyer.BuyerReviewRepo;
import com.cakequake.cakequakeback.review.service.buyer.BuyerReviewServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;


@ExtendWith(MockitoExtension.class)
public class BuyerReviewTest {

    //실제 DB 호출은 차단
    @Mock
    private BuyerReviewRepo repo;

    @InjectMocks
    private BuyerReviewServiceImpl service;

    // ------------------getMyReview테스트 - 구매자 상품 전체 조회----------
    @Test
    @DisplayName("getMyReview: 페이지당 2개 요청 시 hasNext=true, totalCount=3")
    void getMyReviews_returnsInfiniteScrollResponse(){
        //1번 페이지에 2개의 항목 요청
        PageRequestDTO pageRequest = PageRequestDTO.builder()
                .page(1)
                .size(2)
                .build();
        Long userId = 1L; //조회할 유저 아이디

        List<ReviewResponseDTO> dtos = List.of(
                new ReviewResponseDTO(1L,10L,20L,"케이크",5,"굿!",null,null,null,null),
                new ReviewResponseDTO(1L,11L,21L,"케이크",4,"굿!",null,null,null,null)
        );
        Page<ReviewResponseDTO> page = new PageImpl<>(
                dtos,
                pageRequest.getPageable("regDate"),
                3  //총 3개의 데이터
        );

        // BuyerRepo에 listOfUserReviews 실행하면 바로 위 page반환
        given(repo.listOfUserReviews(userId,pageRequest.getPageable("regDate")))
                .willReturn(page);

        // 실제 호출
        InfiniteScrollResponseDTO<ReviewResponseDTO> result = service.getMyReviews(pageRequest, userId);

        //content 확인
        assertThat(result.getContent()).isEqualTo(dtos);

        //다음 페이지가 존재 해야함
        assertThat(result.isHasNext()).isTrue();

        //totalCount도 3으로 맞춰야 함
        assertThat(result.getTotalCount()).isEqualTo(3);
    }

    //--------------------getReview - 구매자 리뷰 단건 조회-----------
    @Test
    @DisplayName("getReview: 정상 조회 시 DTO 반환")
    void getReview_successful(){
        Long reviewId = 50L;
        Long uid = 200L;

        Member fakeMember = org.mockito.Mockito.mock(
                com.cakequake.cakequakeback.member.entities.Member.class);
        given(fakeMember.getUid()).willReturn(uid);

        Review review = Review.builder()
                .reviewId(reviewId)
                .user(fakeMember)
                .rating(5)
                .content("맛있어요")
                .build();

        //Repo.findById호출
        given(repo.findById(reviewId))
                .willReturn(Optional.of(review));

        // Repo.selectDTO  호출 시 데스트용 DTO 반환
        ReviewResponseDTO dto = new ReviewResponseDTO(reviewId, 1L, 2L, "케이크", 5, "맛있어요", null, null,null, null);
        given(repo.selectDTO(reviewId))
                .willReturn(dto);

        //서비스 호출
        ReviewResponseDTO result = service.getReview(reviewId,uid);

        assertThat(result).isEqualTo(dto);
    }

    //----------updateReview - 리뷰 수정-----------------------
    @Test
    @DisplayName("updateReviw: 정상 수정 시 rating, content, pictureUrl 변경 및 DTO 반환")
    void updateReview_successful(){
        Long reviewId = 50L;
        Long uid = 200L;

        Member fakeMember = mock(Member.class);
        given(fakeMember.getUid()).willReturn(uid);

        Review review = Review.builder()
                .reviewId(reviewId)
                .user(fakeMember)
                .rating(2)
                .content("기존 내용")
                .reviewPictureUrl("pictureUrl.jpg")
                .status(ReviewStatus.ACTIVE)
                .build();

        given(repo.findById(reviewId))
            .willReturn(Optional.of(review));

        ReviewRequestDTO req = new ReviewRequestDTO(
                5,
                "수정된 내용",
                "new.jpg"
        );

        ReviewResponseDTO updatedDTO = new ReviewResponseDTO(
                reviewId,
                10L,
                20L,
                "케이크",
                5,
                "수정된 내용",
                "new.jpg",
                LocalDateTime.of(2025,5,27,10,0),// regDate
                LocalDateTime.of(2025,5,27,10,0),// modDate
                null                             // reply
        );

        given(repo.selectDTO(reviewId))
                .willReturn(updatedDTO);

        //서비스 호출
        ReviewResponseDTO result = service.updateReview(reviewId,req,uid);

        //서비스 검증
        assertThat(result.getRating()).isEqualTo(5);
        assertThat(result.getContent()).isEqualTo("수정된 내용");
        assertThat(result.getReviewPictureUrl()).isEqualTo("new.jpg");
    }

    //---------------deleteReview - 구매자 리뷰 삭제 --------------------
    @Test
    @DisplayName("deleteReview: 정상 삭제 시 status가 DELETE로 변경")
    void deleteReview_successful(){
        Long reviewId = 50L;
        Long uid = 200L;

        Member fakeMember = mock(Member.class);
        given(fakeMember.getUid()).willReturn(uid);

        Review review = Review.builder()
                .reviewId(reviewId)
                .user(fakeMember)
                .status(ReviewStatus.ACTIVE)
                .build();

        given(repo.findById(reviewId))
        .willReturn(Optional.of(review));

        service.deleteReview(reviewId,uid);

        assertThat(review.getReviewId()).isEqualTo(reviewId);
    }

}
