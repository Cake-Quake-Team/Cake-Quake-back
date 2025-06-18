package com.cakequake.cakequakeback.procurement.repo;

import com.cakequake.cakequakeback.procurement.entities.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientRepo extends JpaRepository<Ingredient, Long> {
}
