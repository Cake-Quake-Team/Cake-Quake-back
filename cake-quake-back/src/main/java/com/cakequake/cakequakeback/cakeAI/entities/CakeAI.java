package com.cakequake.cakequakeback.cakeAI.entities;

import com.cakequake.cakequakeback.common.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ai_chat_log")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class CakeAI extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 질문 내용
    @Column(columnDefinition = "TEXT", nullable = false)
    private String question;

    // AI가 생성한 답변 (응답)
    @Column(columnDefinition = "TEXT", nullable = false)
    private String answer;

    // 대화 유형 (ex: "chat", "recommend_options", "recommend_lettering", "recommend_image")
    @Column(nullable = false)
    private String category;

    // 대화 세션 구분 할 ID
    @Column(nullable = false)
    private String sessionId;

    public void updateAnswer(String answer) {
        this.answer = answer;
    }

}
