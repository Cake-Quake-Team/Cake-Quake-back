package com.cakequake.cakequakeback.procurement.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "procurement_items")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcurementItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long procurementItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procurementId", nullable = false)
    private Procurement procurement;

    @Column(nullable = false)
    private Long ingredientId;

    @Column(nullable = false)
    private Integer quantity;

}
