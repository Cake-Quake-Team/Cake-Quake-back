package com.cakequake.cakequakeback.procurement.dto.ingredient;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class IngredientResponseDTO {
    private Long ingredientId;
    private String name;
    private String unit;
    private BigDecimal pricePerUnit;
    private String description;
    private Integer stockQuantity;
    private LocalDateTime redDate;
    private LocalDateTime modDate;
}
