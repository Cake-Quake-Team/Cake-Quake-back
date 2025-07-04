package com.cakequake.cakequakeback.like.service;

import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.like.dto.shop.GetShopLikes;
import com.cakequake.cakequakeback.like.dto.shop.ToggleShopLike;
import com.cakequake.cakequakeback.like.entities.ShopLike;
import com.cakequake.cakequakeback.like.repo.ShopLikeRepository;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.member.repo.MemberRepository;
import com.cakequake.cakequakeback.shop.entities.Shop;
import com.cakequake.cakequakeback.shop.repo.ShopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ShopLikeServiceImpl implements ShopLikeService {

    private final ShopLikeRepository shopLikeRepository;
    private final MemberRepository memberRepository;
    private final ShopRepository shopRepository;

    @Override
    public ToggleShopLike.Response toggleShopLike(String userId, ToggleShopLike.Request request) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_UID));

        Shop shop = shopRepository.findById(request.getShopId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_SHOP_ID));

        Optional<ShopLike> existingShopLike = shopLikeRepository.findByMemberUidAndShopShopId(member.getUid(), shop.getShopId());

        boolean isLiked;
        String message;
        Long shopLikeId = null;

        if (existingShopLike.isPresent()) {
            // 이미 찜한 상태 -> 찜 취소 (삭제)
            shopLikeRepository.delete(existingShopLike.get());
            isLiked = false;
            message = "매장 찜을 취소했습니다.";
            log.info("매장 찜 취소: 회원 ID {} -> 매장 ID {}", userId, shop.getShopId());
        } else {
            // 찜하지 않은 상태 -> 찜 추가
            ShopLike newShopLike = ShopLike.builder()
                    .member(member)
                    .shop(shop)
                    .build();
            ShopLike savedShopLike = shopLikeRepository.save(newShopLike);
            shopLikeId = savedShopLike.getShopLikeId();
            isLiked = true;
            message = "매장을 찜했습니다.";
            log.info("매장 찜 추가: 회원 ID {} -> 매장 ID {}, 찜 ID {}", userId, shop.getShopId(), shopLikeId);
        }

        // 해당 매장의 총 찜 개수 업데이트 (선택 사항)
        long totalShopLikesCount = shopLikeRepository.countByShopShopId(shop.getShopId());

        return ToggleShopLike.Response.builder()
                .shopLikeId(shopLikeId)
                .shopId(shop.getShopId())
                .isLiked(isLiked)
                .message(message)
                .totalShopLikesCount((int) totalShopLikesCount) // int로 캐스팅
                .build();
    }

    @Override
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션
    public GetShopLikes.Response getLikedShops(String userId) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_UID));

        List<ShopLike> likedShopEntities = shopLikeRepository.findByMemberUid(member.getUid());

        List<GetShopLikes.ShopLikeInfo> likedShopInfos = likedShopEntities.stream()
                .map(shopLike -> GetShopLikes.ShopLikeInfo.builder()
                        .shopLikeId(shopLike.getShopLikeId())
                        .shopId(shopLike.getShop().getShopId())
                        .shopName(shopLike.getShop().getShopName())
                        .shopAddress(shopLike.getShop().getAddress()) // Shop 엔티티에 address 필드가 있다고 가정
                        .thumbnailUrl(shopLike.getShop().getThumbnailImageUrl()) // Shop 엔티티에 thumbnailUrl 필드가 있다고 가정
                        .openTime(shopLike.getShop().getOpenTime().toString()) // LocalTime -> String
                        .closeTime(shopLike.getShop().getCloseTime().toString()) // LocalTime -> String
                        .closeDays(shopLike.getShop().getCloseDays()) // String 그대로
                        .build())
                .collect(Collectors.toList());

        return GetShopLikes.Response.builder()
                .likedShops(likedShopInfos)
                .totalElements(likedShopInfos.size())
                .build();
    }

    @Override
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션
    public boolean isShopLiked(String userId, Long shopId) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_UID));

        return shopLikeRepository.findByMemberUidAndShopShopId(member.getUid(), shopId).isPresent();
    }
}