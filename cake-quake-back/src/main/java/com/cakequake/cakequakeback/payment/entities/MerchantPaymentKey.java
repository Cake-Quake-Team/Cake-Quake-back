package com.cakequake.cakequakeback.payment.entities;

import com.cakequake.cakequakeback.common.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "merchant_payment_key")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantPaymentKey extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name ="shopId", nullable = false)
    private Long shopId;

    //PG사 구분
    @Column(length =20 , nullable = false)
    private String provider;

    //암호화된 API키
    @Column(name="encryptedApiKey" ,columnDefinition="TEXT",nullable = false)
    private String encryptedApiKey;

    // adminKey등 -> 카카오페이 전용 null가능
    @Column(columnDefinition = "TEXT")
    private String encryptedSecret;

    @Column(nullable = false)
    private boolean isActive;


}
