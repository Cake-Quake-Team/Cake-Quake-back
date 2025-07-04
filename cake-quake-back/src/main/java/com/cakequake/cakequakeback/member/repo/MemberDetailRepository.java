package com.cakequake.cakequakeback.member.repo;

import com.cakequake.cakequakeback.member.entities.MemberDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberDetailRepository extends JpaRepository<MemberDetail, Long> {
}
