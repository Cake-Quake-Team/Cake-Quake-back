package com.cakequake.cakequakeback.procurement.entities;

import com.cakequake.cakequakeback.common.entities.BaseEntity;
import com.cakequake.cakequakeback.review.entities.ReviewStatus;
import com.cakequake.cakequakeback.shop.entities.Shop;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "procurement")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Procurement extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long procurementId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shopId", nullable =false)
    private Shop shop;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcurementStatus status = ProcurementStatus.REQUESTED;

    @Column(columnDefinition = "TEXT")
    private String note;

    private LocalDate scheduledDate;


    public void updateScheduledDate(LocalDate scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public void updateStatus(ProcurementStatus status) {
        this.status = status;
    }
}
