package com.cakequake.cakequakeback.qna.service;

import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.member.entities.MemberRole;
import com.cakequake.cakequakeback.qna.dto.QnAAdminRequestDTO;
import com.cakequake.cakequakeback.qna.dto.QnARequestDTO;
import com.cakequake.cakequakeback.qna.dto.QnAResponseDTO;
import com.cakequake.cakequakeback.qna.entities.QnAStatus;
import com.cakequake.cakequakeback.qna.entities.QnAType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QnAService {
    //내 문의 작성
    Long createQnA(QnARequestDTO qnARequestDTO, Long uid);

    //내 문의 무한 스크롤 조회
    InfiniteScrollResponseDTO<QnAResponseDTO> getMyQnAList(PageRequestDTO pageRequestDTO, Long uid);

    //내 문의 상세 조회
    QnAResponseDTO getMyQnA(Long qnaId, Long uid);

    //내 문의 수정
    QnAResponseDTO updateQnA(Long qnaId, QnARequestDTO qnaRequestDTO, Long uid);

    //내 문의 삭제
    void deleteQnA(Long qnaId, Long uid);

    //---------------관리자
    //유형별 문의 페이징 조회
    Page<QnAResponseDTO> listByType(QnAType type, Pageable pageable);

    //상태별 문의 페이징 조회
    Page<QnAResponseDTO> listByStatus(QnAStatus status, Pageable pageable);

    //전체 문의 최신순 조회
    Page<QnAResponseDTO> listAll(Pageable pageable);

    //답변 등록
    void respondToQnA(QnAAdminRequestDTO qnAAdminRequestDTO);

    //작성자 역활별 페이징 조회
    Page<QnAResponseDTO> listBuAuthorRole(MemberRole role, Pageable pageable);

    //QnA 상세 조회
    QnAResponseDTO getDetailForAdmin(Long qnaId);
}
