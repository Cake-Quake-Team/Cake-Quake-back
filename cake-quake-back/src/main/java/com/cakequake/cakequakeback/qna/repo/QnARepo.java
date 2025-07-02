package com.cakequake.cakequakeback.qna.repo;

import com.cakequake.cakequakeback.member.entities.MemberRole;
import com.cakequake.cakequakeback.qna.entities.QnA;
import com.cakequake.cakequakeback.qna.entities.QnAStatus;
import com.cakequake.cakequakeback.qna.entities.QnAType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QnARepo extends JpaRepository<QnA, Long> {

    //회원별 페이징 조회
    Page<QnA> findByMember_Uid(Long memberUid, Pageable pageable);

    //유형별 페이징 조회
    Page<QnA> findByQnAType(QnAType type, Pageable pageable);

    //상태별 페이징 조회
    Page<QnA> findByStatus(QnAStatus status, Pageable pageable);

    //전체 최신순 페이징 조회
    Page<QnA> findAll(Pageable pageable);

    Page<QnA> findByMember_Role(MemberRole role, Pageable pageable);
}
