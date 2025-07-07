package com.cakequake.cakequakeback.member.repo;

import com.cakequake.cakequakeback.member.dto.buyer.BuyerProfileResponseDTO;
import com.cakequake.cakequakeback.member.dto.seller.SellerResponseDTO;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.member.entities.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

/*
    2025.06.19 수정
    * 탈퇴여부 status ACTIVE, WITHDRAWN 추가.
 */
public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByUserId(String userId);

    // 탈퇴 여부 상관 없이 모든 사용자 조회
    Optional<Member> findByUserId(String userId);

    // 활성 사용자만 조회 (로그인, 인증용)
    Optional<Member> findByUserIdAndStatus(String userId, MemberStatus status);

    boolean existsByPhoneNumber(String phoneNumber);

    @Query("""
        select new com.cakequake.cakequakeback.member.dto.seller.SellerResponseDTO(
            u.uid, u.userId, u.uname, u.phoneNumber, u.role
        ) 
        from Member u 
        where u.uid = :uid and u.status = 'ACTIVE'
    """)
    Optional<SellerResponseDTO> sellerGetOne(Long uid);

    @Query("""
        select new com.cakequake.cakequakeback.member.dto.buyer.BuyerProfileResponseDTO(
            u.uid, u.userId, u.uname, u.phoneNumber, u.alarm, u.role
        ) 
        from Member u 
        where u.uid = :uid and u.status = 'ACTIVE'
    """)
    Optional<BuyerProfileResponseDTO> buyerGetOne(Long uid);

    Optional<Member> findByUidAndStatus(Long uid, MemberStatus status);

}
