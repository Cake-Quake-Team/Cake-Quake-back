package com.cakequake.cakequakeback.payment.repo;

import com.cakequake.cakequakeback.payment.dto.PaymentResponseDTO;
import com.cakequake.cakequakeback.payment.entities.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentRepo extends JpaRepository<Payment,Long> {

    //본인 결제 목록 전체 조회 -> DTO리스트로 바로 변환
    @Query("""
    SELECT new com.cakequake.cakequakeback.payment.dto.PaymentResponseDTO(
    p.paymentId,
    p.provider,
    p.status,
    p.amount,
    p.transactionId,
    p.regDate,
    p.completedAt,
    p.cancelReason,
    p.refundAt,
    p.refundReason,
    p.redirectUrl,
    p.paymentUrl
    )
    FROM Payment p
    WHERE p.member.uid = :uid
    ORDER BY p.regDate DESC
""")
    List<PaymentResponseDTO> selectPaymentListDTO(
            @Param("uid") Long uid
    );

    //본인 결제 단건 조회 -> DTO 바로 반환
    @Query("""
    SELECT new com.cakequake.cakequakeback.payment.dto.PaymentResponseDTO(
              p.paymentId,
              p.provider,
              p.status,
              p.amount,
              p.transactionId,
              p.regDate,
              p.completedAt,
              p.cancelReason,
              p.refundAt,
              p.refundReason,
              p.redirectUrl,
              p.paymentUrl
    )
    From Payment p
    WHERE p.paymentId = :paymentId
    And p.member.uid =:uid
""")
    PaymentResponseDTO selectPaymentDTO(
            @Param("paymentId") Long paymentId,
            @Param("uid") Long uid
    );

    // 본인 결제 엔티티 단건 조회 (payment_id, member.userId 조건)
    Optional<Payment> findByPaymentIdAndMemberUid(Long paymentId, Long userId);

    // 본인 결제 엔티티 페이징 조회 (member.userId 조건)
    Page<Payment> findAllByMemberUid(Long memberId, Pageable pageable);

    // 한 주문에 대한 결제 조회 (order.orderId 조건)
    Optional<Payment> findByOrderOrderId(Long orderId);

    // PG사 거래 식별자(transactionId)으로 조회
    Optional<Payment> findByTransactionId( String transactionId);

    /** 매장별 전체 결제 목록 조회 → DTO 바로 반환 */
//    @Query("""
//      SELECT new com.cakequake.cakequakeback.payment.dto.PaymentResponseDTO(
//        p.paymentId,
//        p.provider,
//        p.status,
//        p.amount,
//        p.transactionId,
//        p.regDate,
//        p.completedAt,
//        p.cancelReason,
//        p.refundAt,
//        p.refundReason,
//        null,
//        null
//      )
//      FROM Payment p
//      WHERE p.order.shop.shopId = :shopId
//      ORDER BY p.regDate DESC
//    """)
//    List<PaymentResponseDTO> selectByShopId(@Param("shopId") Long shopId);

//    /** 매장별 결제 엔티티 페이징 조회 */
//    Page<Payment> findAllByOrderShopId(Long shopId, Pageable pageable);


}
