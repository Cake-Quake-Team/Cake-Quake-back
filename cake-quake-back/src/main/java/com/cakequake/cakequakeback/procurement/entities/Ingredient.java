package com.cakequake.cakequakeback.procurement.entities;

import com.cakequake.cakequakeback.common.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "ingredient")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ingredient extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ingredientId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false,length = 20)
    private String unit;

    @Column(nullable = false)
    private BigDecimal pricePerUnit;

    @Column(length = 255)
    private String description;

    public void updateName(String name) {
        this.name = name;
    }

    public void updateUnit(String unit) {
        this.unit = unit;
    }

    public void updateDescription(String description) {
        this.description = description;
    }

    public void updatePricePerUnit(BigDecimal pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }
}
