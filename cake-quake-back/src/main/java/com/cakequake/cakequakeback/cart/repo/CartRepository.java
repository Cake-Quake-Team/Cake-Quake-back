package com.cakequake.cakequakeback.cart.repo;

import com.cakequake.cakequakeback.cart.entities.Cart;
import com.cakequake.cakequakeback.member.entities.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    // Cart 엔티티의 'member' 필드를 기준으로 조회
    Optional<Cart> findByMember(Member member);

    // Member 엔티티를 기준으로 Cart 존재 여부 확인
    //boolean existsByMember(Member member);

    // Cart 엔티티의 'member' 필드를 기준으로 삭제
    //void deleteByMember(Member member);
}
