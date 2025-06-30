package com.cakequake.cakequakeback.procurement.dto.ingredient;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class IngredientRequestDTO {
    @NotBlank(message = "재료명은 필수입니다.")
    private String name;

    @NotBlank(message = "단위는 필수입니다.")
    private String unit;

    @NotNull
    private BigDecimal pricePerUnit;

    private String description;

    private Integer stockQuantity;
}
