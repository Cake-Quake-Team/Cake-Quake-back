package com.cakequake.cakequakeback.like.service;

import com.cakequake.cakequakeback.cake.item.entities.CakeItem;
import com.cakequake.cakequakeback.cake.item.repo.CakeItemRepository;
import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.like.dto.cake.GetLikes;
import com.cakequake.cakequakeback.like.dto.cake.ToggleLike;
import com.cakequake.cakequakeback.like.entities.Like;
import com.cakequake.cakequakeback.like.repo.LikeRepository;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.member.repo.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Spring의 @Transactional 사용

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j // 로깅 사용을 위한 어노테이션
@Service
@RequiredArgsConstructor
@Transactional // 클래스 레벨에 @Transactional 적용
public class LikeServiceImpl implements LikeService {

    private final LikeRepository likeRepository;
    private final MemberRepository memberRepository;
    private final CakeItemRepository cakeItemRepository;

    @Override
    public ToggleLike.Response toggleLike(String userId, ToggleLike.Request request) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_UID));

        CakeItem cakeItem = cakeItemRepository.findById(request.getCakeItemId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_CAKE_ITEM, "케이크 상품을 찾을 수 없습니다."));

        Optional<Like> existingLike = likeRepository.findByMemberUidAndCakeItemCakeId(member.getUid(), cakeItem.getCakeId());

        boolean isLiked;
        String message;
        Long likeId = null;

        if (existingLike.isPresent()) {
            // 이미 찜한 상태 -> 찜 취소 (삭제)
            likeRepository.delete(existingLike.get());
            isLiked = false;
            message = "찜을 취소했습니다.";
            log.info("찜 취소: 회원 ID {} -> 케이크 ID {}", userId, cakeItem.getCakeId());
        } else {
            // 찜하지 않은 상태 -> 찜 추가
            Like newLike = Like.builder()
                    .member(member)
                    .cakeItem(cakeItem)
                    .build();
            Like savedLike = likeRepository.save(newLike);
            likeId = savedLike.getLikeId();
            isLiked = true;
            message = "찜했습니다.";
            log.info("찜 추가: 회원 ID {} -> 케이크 ID {}, 찜 ID {}", userId, cakeItem.getCakeId(), likeId);
        }

        // 해당 케이크의 총 찜 개수 업데이트 (선택 사항)
        long totalLikesCount = likeRepository.countByCakeItemCakeId(cakeItem.getCakeId());

        return ToggleLike.Response.builder()
                .likeId(likeId)
                .cakeItemId(cakeItem.getCakeId())
                .isLiked(isLiked)
                .message(message)
                .totalLikesCount((int) totalLikesCount) // int로 캐스팅
                .build();
    }



    @Override
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션
    public GetLikes.Response getLikedItems(String userId) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_UID));

        List<Like> likedEntities = likeRepository.findByMemberUid(member.getUid());

        List<GetLikes.LikeInfo> likedItemInfos = likedEntities.stream()
                .map(like -> GetLikes.LikeInfo.builder()
                        .likeId(like.getLikeId())
                        .cakeItemId(like.getCakeItem().getCakeId())
                        .cakeName(like.getCakeItem().getCname())
                        .thumbnailUrl(like.getCakeItem().getThumbnailImageUrl())
                        .price(like.getCakeItem().getPrice())
                        .shopId(like.getCakeItem().getShop().getShopId())
                        .shopName(like.getCakeItem().getShop().getShopName())
                        .build())
                .collect(Collectors.toList());

        return GetLikes.Response.builder()
                .likedItems(likedItemInfos)
                .totalCount(likedItemInfos.size())
                .build();
    }

    @Override
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션
    public boolean isCakeItemLiked(String userId, Long cakeItemId) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_UID));

        return likeRepository.findByMemberUidAndCakeItemCakeId(member.getUid(), cakeItemId).isPresent();
    }
}