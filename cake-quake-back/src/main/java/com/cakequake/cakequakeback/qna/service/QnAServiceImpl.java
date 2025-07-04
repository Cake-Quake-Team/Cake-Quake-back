package com.cakequake.cakequakeback.qna.service;

import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.member.entities.MemberRole;
import com.cakequake.cakequakeback.member.repo.MemberRepository;
import com.cakequake.cakequakeback.qna.dto.QnAAdminRequestDTO;
import com.cakequake.cakequakeback.qna.dto.QnARequestDTO;
import com.cakequake.cakequakeback.qna.dto.QnAResponseDTO;
import com.cakequake.cakequakeback.qna.entities.QnA;
import com.cakequake.cakequakeback.qna.entities.QnAStatus;
import com.cakequake.cakequakeback.qna.entities.QnAType;
import com.cakequake.cakequakeback.qna.repo.QnARepo;
import com.cakequake.cakequakeback.qna.validator.QnAValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class QnAServiceImpl implements QnAService {

    private final QnARepo qnaRepo;
    private final MemberRepository MemberRepo;
    private final QnAValidator qnaValidator;

    //QnA생성
    @Override
    public Long createQnA(QnARequestDTO qnARequestDTO, Long uid) {
        Member member = qnaValidator.validaterMemberExists(uid);

        QnA qna = QnA.builder()
                .member(member)
                .qnAType(qnARequestDTO.getQnAType())
                .title(qnARequestDTO.getTitle())
                .content(qnARequestDTO.getContent())
                .build();

        return qnaRepo.save(qna).getQnaId();
    }

    //내 문의 리스트 조회
    @Override
    @Transactional(readOnly = true)
    public InfiniteScrollResponseDTO<QnAResponseDTO> getMyQnAList(PageRequestDTO pageRequestDTO, Long uid) {
       qnaValidator.validaterMemberExists(uid);

       Pageable pageable = pageRequestDTO.getPageable("regDate");
       Page<QnA> page = qnaRepo.findByMember_Uid(uid, pageable);

        List<QnAResponseDTO> content = page.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return InfiniteScrollResponseDTO.<QnAResponseDTO>builder()
                .content(content)
                .hasNext(page.hasNext())
                .totalCount((int)page.getTotalElements())
                .build();
    }

    //내문의 단건 조회
    @Override
    @Transactional(readOnly = true)
    public QnAResponseDTO getMyQnA(Long qnaId, Long uid) {
        QnA qna = qnaValidator.validateOwner(qnaId, uid);
        return toDTO(qna);
    }

    //문의 수정
    @Override
    public QnAResponseDTO updateQnA(Long qnaId, QnARequestDTO qnaRequestDTO, Long uid) {
        QnA qna = qnaValidator.validateUpdatealbe(qnaId, uid);
        qna.updateTitle(qnaRequestDTO.getTitle());
        qna.updateContent(qnaRequestDTO.getContent());

        return toDTO(qna);
    }

    //문의 삭제
    @Override
    public void deleteQnA(Long qnaId, Long uid) {
    QnA qna = qnaValidator.validateDeletable(qnaId, uid);
    qnaRepo.delete(qna);
    }

    //----------------관리자

    //관리자 조회 - 유형별
    @Override
    @Transactional(readOnly = true)
    public Page<QnAResponseDTO> listByType(QnAType type, Pageable pageable) {
        return qnaRepo.findByQnAType(type,pageable)
                .map(this::toDTO);
    }

    //관리자 조회 - 상태별
    @Override
    @Transactional(readOnly = true)
    public Page<QnAResponseDTO> listByStatus(QnAStatus status, Pageable pageable) {
        return qnaRepo.findByStatus(status,pageable)
                .map(this::toDTO);
    }

    //관지라 조회 - 전체 최신순
    @Override
    @Transactional(readOnly = true)
    public Page<QnAResponseDTO> listAll(Pageable pageable) {
        return qnaRepo.findAll(pageable)
                .map(this::toDTO);
    }

    //관리자 답변 등록
    @Override
    public void respondToQnA(QnAAdminRequestDTO qnAAdminRequestDTO) {
        QnA qna = qnaValidator.validateRespondable(qnAAdminRequestDTO.getQnaId());
        qna.startProgress();
        qna.respond(qnAAdminRequestDTO.getAdminResponse());
    }

    //등록 role에따른 분류
    @Override
    @Transactional(readOnly = true)
    public Page<QnAResponseDTO> listBuAuthorRole(MemberRole role, Pageable pageable) {
        return qnaRepo.findByMember_Role(role, pageable)
                .map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public QnAResponseDTO getDetailForAdmin(Long qnaId) {
        QnA qna = qnaValidator.validateExists(qnaId);

        return toDTO(qna);
    }


    private QnAResponseDTO toDTO(QnA qna) {
        return QnAResponseDTO.builder()
                .qnaId(qna.getQnaId())
                .memberId(qna.getMember().getUid())
                .qnAType(qna.getQnAType())
                .title(qna.getTitle())
                .content(qna.getContent())
                .adminResponse(qna.getAdminResponse())
                .status(qna.getStatus())
                .regDate(qna.getRegDate())
                .modDate(qna.getModDate())
                .build();
    }
}
