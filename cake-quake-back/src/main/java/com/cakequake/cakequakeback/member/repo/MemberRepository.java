package com.cakequake.cakequakeback.member.repo;

import com.cakequake.cakequakeback.member.dto.buyer.BuyerProfileResponseDTO;
import com.cakequake.cakequakeback.member.dto.seller.SellerResponseDTO;
import com.cakequake.cakequakeback.member.entities.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByUserId(String userId);

    Optional<Member> findByUserId(String userId);

    boolean existsByPhoneNumber(String phoneNumber);

    @Query("select new  com.cakequake.cakequakeback.member.dto.seller.SellerResponseDTO(u.uid, u.userId, u.uname, u.phoneNumber, u.role) from Member u where u.uid = :uid ")
    Optional<SellerResponseDTO> sellerGetOne(Long uid);

    @Query("select new  com.cakequake.cakequakeback.member.dto.buyer.BuyerProfileResponseDTO(u.uid, u.userId, u.uname, u.phoneNumber, u.alarm, u.role) from Member u where u.uid = :uid ")
    Optional<BuyerProfileResponseDTO> buyerGetOne(Long uid);

}
