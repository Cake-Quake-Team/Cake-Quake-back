package com.cakequake.cakequakeback.like.repo;

import com.cakequake.cakequakeback.like.entities.ShopLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface ShopLikeRepository extends JpaRepository<ShopLike, Long> {

    /* 특정 회원과 특정 매장에 대한 찜 기록을 조회. 매장 찜 토글 로직에서 사용 */
    Optional<ShopLike> findByMemberUidAndShopShopId(Long memberUid, Long shopId);

    /* 특정 회원이 찜한 모든 매장 목록을 조회. 찜한 매장 목록 조회 시 사용 */
    List<ShopLike> findByMemberUid(Long memberUid);

    /* 특정 매장에 대한 총 찜 개수를 조회 */
    long countByShopShopId(Long shopId);
}