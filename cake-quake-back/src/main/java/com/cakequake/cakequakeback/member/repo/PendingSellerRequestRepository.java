package com.cakequake.cakequakeback.member.repo;

import com.cakequake.cakequakeback.member.entities.PendingSellerRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PendingSellerRequestRepository extends JpaRepository<PendingSellerRequest, Long>, PendingSellerRequestCustomRepository {

    Optional<Object> findByUserId(String userId);
}
