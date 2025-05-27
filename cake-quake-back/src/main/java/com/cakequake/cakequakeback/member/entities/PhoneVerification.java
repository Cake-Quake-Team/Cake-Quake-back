package com.cakequake.cakequakeback.member.entities;

import com.cakequake.cakequakeback.common.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// 휴대전화 인증 (시뮬레이션)
@Entity
@Table(name = "phone_verification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PhoneVerification extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq_gen")
    @SequenceGenerator(
            name = "user_seq_gen",       // JPA 내부에서 쓸 식별자
            sequenceName = "user_seq",   // DB에 생성된 시퀀스 이름
            initialValue = 1,            // 시퀀스 START WITH 값
            allocationSize = 50          // 한 번에 미리 가져올 시퀀스 수
    )
    private Long id;

    @Column(nullable = false, length = 20, unique = true)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VerificationType type;

    @Column(nullable = false, length = 6)
    private String code;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;    // 인증번호 유효 만료 시간

    @Builder.Default
    @Column(nullable = false)
    private boolean verified = false;

    // 인증번호 재발급 시 사용
    public void changeCode(String code, LocalDateTime expiresAt) {
        this.code = code;
        this.expiresAt = expiresAt;
        this.verified = false; // 재발급 시 검증 초기화
    }

    // 인증 성공 처리
    public void changeVerified(boolean verified) {
        this.verified = verified;
    }

}
