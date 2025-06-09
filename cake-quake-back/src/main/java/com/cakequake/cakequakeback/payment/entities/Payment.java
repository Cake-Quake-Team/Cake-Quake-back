package com.cakequake.cakequakeback.payment.entities;

import com.cakequake.cakequakeback.common.entities.BaseEntity;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.order.entities.CakeOrder;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.catalina.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "orderId", nullable = false)
    private CakeOrder order;

    @ManyToOne(fetch=FetchType.LAZY, optional = false)
    @JoinColumn(name = "uid", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", length=20 , nullable=false)
    private PaymentProvider provider;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private PaymentStatus status;

    @Positive
    @Column(name= "amount", nullable = false)
    private BigDecimal amount;

    //PG사 거래 식별자 (kakao: tid , Toss:paymentKey)
    @Column( length =100, nullable =false, unique = true )
    private String transactionId;

    //결제 완료 처리 시각
    @Column(name = "completedAt")
    private LocalDateTime completedAt;

    //결제 취소 사유 (Status = CANCLED일 때만 값이 설정)
    @Column(name = "cancelReason", length = 255)
    private String cancelReason;


    //  환불 처리 시각 (환불 기능이 필요할 때 사용)
    @Column(name = "refund_at")
    private LocalDateTime refundAt;


    //환불 사유 (환불 처리 시 설정)
    @Column(name = "refund_reason", length = 255)
    private String refundReason;

    // ────────────────────────────────
    // 아래 두 필드를 추가해야 쿼리에서 p.redirectUrl, p.paymentUrl 참조 가능
    // ────────────────────────────────

    // 카카오페이 결제 키 (redirectUrl)
    @Column( length = 500)
    private String redirectUrl;

    // 토스페이 결제 URL (paymentUrl)
    @Column( length = 500)
    private String paymentUrl;


//    public void setTransactionId(String transactionId) {
//        this.transactionId = transactionId;
//    }
//
//    public void setRedirectUrl(String redirectUrl) {
//        this.redirectUrl = redirectUrl;
//    }

    //상태 변경 메서드
    public void approveByPg(){
        if(this.status != PaymentStatus.READY){
            throw new IllegalStateException("Ready 상태가 아닌 결제는 승인할 수 없습니다.");
        }

        this.status = PaymentStatus.APPROVED;
        this.completedAt = LocalDateTime.now();
    }

    //승인된 결제만 취소 가능
    public void cancelByBuyer(String cancelReason){
        if(this.status != PaymentStatus.APPROVED){
            throw new IllegalStateException("승인된 결제만 취소 가능합니다. 현재 상태: "+ status);
        }
        this.status = PaymentStatus.CANCELLED;
        this.cancelReason = cancelReason;
        this.completedAt = LocalDateTime.now();
    }

    //환불은 승인 또는 이미 취소된 결제만 허용
    public void refundByBuyer(String refundReason){
        if(this.status != PaymentStatus.APPROVED && this.status != PaymentStatus.CANCELLED){
            throw new IllegalStateException("환불 가능한 상태가 아닙니다. 현재 상태: "+ status);
        }
        this.status = PaymentStatus.REFUNDED;
        this.refundAt = LocalDateTime.now();
        this.refundReason = refundReason;

    }
}
