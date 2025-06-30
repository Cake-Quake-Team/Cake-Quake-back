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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "ingredient_name", length = 100, nullable = false)
    private String ingredientName;

    @Column(name = "unit_price", nullable = false)
    private Integer unitPrice;


}
