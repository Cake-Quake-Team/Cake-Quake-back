package com.cakequake.cakequakeback.procurement.service;

import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.procurement.dto.ingredient.IngredientRequestDTO;
import com.cakequake.cakequakeback.procurement.dto.ingredient.IngredientResponseDTO;
import jakarta.persistence.criteria.CriteriaBuilder;

import java.util.List;

public interface IngredientService {
    //전체 재료 목록 조회
    InfiniteScrollResponseDTO<IngredientResponseDTO> getAllIngredients(PageRequestDTO pageRequestDTO);

    //단건 재료 조회
    IngredientResponseDTO getIngredient (Long ingredientId);

    //신규 재료 등록
    IngredientResponseDTO createIngredient(IngredientRequestDTO ingredientRequestDTO);

    //기본 재료 수정
    IngredientResponseDTO updateIngredient(Long ingredientId, IngredientRequestDTO ingredientRequestDTO);

    //재료 삭제
    void deleteIngredient(Long ingredientId);
}
