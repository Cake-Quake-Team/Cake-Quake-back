package com.cakequake.cakequakeback.member.repo;

import com.cakequake.cakequakeback.member.entities.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByUserId(String userId);

    Optional<Member> findByUserId(String userId);

    boolean existsByPhoneNumber(String phoneNumber);

}
