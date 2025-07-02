package com.cakequake.cakequakeback.qna.validator;

import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.member.repo.MemberRepository;
import com.cakequake.cakequakeback.qna.entities.QnA;
import com.cakequake.cakequakeback.qna.entities.QnAStatus;
import com.cakequake.cakequakeback.qna.repo.QnARepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QnAValidator {
    private final QnARepo qnaRepo;
    private final MemberRepository memberRepository;


    //QnA가 존재하는지 검증
    public QnA validateExists(Long qnaId) {
        return qnaRepo.findById(qnaId)
                .orElseThrow(()->new BusinessException(ErrorCode.NOT_FOUND_QNA_ID));
    }

    //회원이 실제 작성자인지 검증
    public QnA validateOwner(Long qnaId,Long Uid) {
        QnA  qna = validateExists(qnaId);
        if(!qna.getMember().getUid().equals(Uid)) {
            throw new BusinessException(ErrorCode.NOT_AUTHORIZED_OTHER);
        }
        return qna;
    }

    //수정 삭제 가능 상태인지 검증
    public QnA validateUpdatealbe(Long qnaId,Long Uid) {
        QnA qna = validateOwner(qnaId,Uid);
        if(qna.getStatus()  == QnAStatus.CLOSED){
            throw new BusinessException(ErrorCode.INVALID_STATUS_UPDATE);
        }
        return qna;
    }

    //삭제 가능 상태인지 검증
    public QnA validateDeletable(Long qnaId,Long Uid) {
        QnA qna = validateOwner(qnaId,Uid);
        if(qna.getStatus()  != QnAStatus.OPEN){
            throw new BusinessException(ErrorCode.INVALID_STATUS_UPDATE);
        }
        return qna;
    }

    //응답 전용: 아직 답변되지 않은 상태인지 검증
    public QnA validateRespondable(Long qnaID){
        QnA qna = validateExists(qnaID);
        if(qna.getStatus() == QnAStatus.CLOSED){
            throw new BusinessException(ErrorCode.ALREADY_QNA_RESPONDED);
        }
        return qna;
    }

    //회원이 실제 존재하는지 검증
    public Member validaterMemberExists(Long Uid){
        return memberRepository.findById(Uid)
                .orElseThrow(()-> new BusinessException(ErrorCode.NOT_FOUND_UID));
    }
}
