package com.cakequake.cakequakeback.like.repo;

import com.cakequake.cakequakeback.like.entities.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    /* 특정 회원과 특정 케이크 상품에 대한 찜 기록을 조회. 찜 토글 로직 사용 */
    Optional<Like> findByMemberUidAndCakeItemCakeId(Long memberUid, Long cakeItemId);

    /* 특정 회원과 특정 케이크 상품에 대한 찜 기록을 삭제. 찜 취소 로직에서 사용 */
    // void deleteByMemberUidAndCakeItemCakeId(Long memberUid, Long cakeItemId); // 직접 삭제 메서드 (사용 시 주의)

    /* 특정 회원이 찜한 모든 케이크 상품 목록을 조회. 찜 목록 조회 시 사용 */
    List<Like> findByMemberUid(Long memberUid);

    /* 특정 케이크 상품에 대한 총 찜 개수를 조회 */
    long countByCakeItemCakeId(Long cakeItemId);
}