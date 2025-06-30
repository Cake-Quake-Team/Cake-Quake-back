package com.cakequake.cakequakeback.procurement.repo;

import com.cakequake.cakequakeback.procurement.entities.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface IngredientRepo extends JpaRepository<Ingredient, Long> {

    @Modifying
    @Transactional
    @Query("""
    UPDATE Ingredient i
    SET i.stockQuantity = i.stockQuantity + :delta
    WHERE i.ingredientId = :ingredientId
      AND ( :delta >= 0 OR i.stockQuantity >= -:delta )
""")
    int adjustStock(
            @Param("ingredientId") Long ingredientId,
            @Param("delta")        int delta
    );
}
