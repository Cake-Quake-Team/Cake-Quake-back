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

    // Cart 엔티티의 'member' 필드를 기준으로 삭제
    void deleteByMember(Member member);
}
