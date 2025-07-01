package com.cakequake.cakequakeback.procurement.entities;

import com.cakequake.cakequakeback.common.entities.BaseEntity;
import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;

@Entity
@Table(name = "ingredient")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE ingredient SET active = false WHERE ingredient_id = ?")
@Where(clause = "active = true")
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

    @Column(nullable = false)
    private Integer stockQuantity;

    // 소프트 삭제용 플래그
    @Builder.Default
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean active = true;

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

    public void updateStockQuantity(Integer stockQuantity) {
        if(stockQuantity <0){
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }
        this.stockQuantity = stockQuantity;
    }


    public void  IncreaseStockQuantity(int amount) {
        this.stockQuantity += amount;
    }

    public void DecreaseStockQuantity(int amount) {
        if(this.stockQuantity < amount) {
            throw new BusinessException(ErrorCode.NOT_ENOUGH_STOCK);
        }
        this.stockQuantity -= amount;
    }

}
