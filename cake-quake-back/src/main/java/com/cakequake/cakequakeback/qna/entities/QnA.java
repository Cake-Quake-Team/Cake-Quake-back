package com.cakequake.cakequakeback.qna.entities;

import com.cakequake.cakequakeback.common.entities.BaseEntity;
import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.member.entities.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="qna")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QnA extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long qnaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uid", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private QnAType qnAType;

    @Column(length = 200, nullable = false)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    //관리자 답변
    @Lob
    @Column
    private String adminResponse;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(length =  20, nullable = false)
    private QnAStatus status = QnAStatus.OPEN;

    //도메인 행동 메서드
    //처리 시작
    public void startProgress(){
        if(this.status != QnAStatus.OPEN){
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS);
        }
        this.status = QnAStatus.IN_PROGRESS;
    }

    public void respond(String response){
        if(this.status != QnAStatus.IN_PROGRESS && this.status != QnAStatus.OPEN){
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS);
        }
        this.adminResponse = response;
        this.status = QnAStatus.CLOSED;
    }


    public void updateContent(String content) {
        this.content = content;
    }

    public void updateTitle(String title) {
        this.title = title;
    }
}
