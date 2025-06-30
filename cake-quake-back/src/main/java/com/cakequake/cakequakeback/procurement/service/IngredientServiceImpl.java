package com.cakequake.cakequakeback.procurement.service;

import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.procurement.dto.ingredient.IngredientRequestDTO;
import com.cakequake.cakequakeback.procurement.dto.ingredient.IngredientResponseDTO;
import com.cakequake.cakequakeback.procurement.entities.Ingredient;
import com.cakequake.cakequakeback.procurement.repo.IngredientRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class IngredientServiceImpl implements IngredientService {

    private final IngredientRepo ingredientRepo;


    @Override
    @Transactional(readOnly = true)
    public InfiniteScrollResponseDTO<IngredientResponseDTO> getAllIngredients(PageRequestDTO pageRequestDTO) {

        Pageable pageable = pageRequestDTO.getPageable("ingredientId");

        Page<Ingredient> page = ingredientRepo.findAll(pageable);

        //Entity -> DTO 매핑
        var dtoPage = page.map(this :: toResponseDTO);

        return InfiniteScrollResponseDTO.<IngredientResponseDTO>builder()
                .content(dtoPage.getContent())
                .hasNext(dtoPage.hasNext())
                .totalCount((int) dtoPage.getTotalElements())
                .build();
    }

    //단건 제품 조회
    @Override
    public IngredientResponseDTO getIngredient(Long ingredientId) {
        Ingredient ing = ingredientRepo.findById(ingredientId)
                .orElseThrow(()-> new BusinessException(
                        ErrorCode.NOT_FOUND_PRODUCT_ID
                ));

        return toResponseDTO(ing);
    }


    //신규 재료 등록
    @Override
    public IngredientResponseDTO createIngredient(IngredientRequestDTO ingredientRequestDTO) {
        Ingredient ing = Ingredient.builder()
                .name(ingredientRequestDTO.getName())
                .unit(ingredientRequestDTO.getUnit())
                .pricePerUnit(ingredientRequestDTO.getPricePerUnit())
                .description(ingredientRequestDTO.getDescription())
                .stockQuantity(ingredientRequestDTO.getStockQuantity() != null ? ingredientRequestDTO.getStockQuantity() : 0)
                .build();
        Ingredient saved = ingredientRepo.save(ing);
        return toResponseDTO(saved);
    }

    //기존 재료 정보 수정
    @Override
    public IngredientResponseDTO updateIngredient(Long ingredientId, IngredientRequestDTO ingredientRequestDTO) {
        Ingredient ing = ingredientRepo.findById(ingredientId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.NOT_FOUND_PRODUCT_ID
                ));
        ing.updateName(ingredientRequestDTO.getName());
        ing.updateDescription(ingredientRequestDTO.getDescription());
        ing.updateUnit(ingredientRequestDTO.getUnit());
        ing.updatePricePerUnit(ingredientRequestDTO.getPricePerUnit());
        if(ingredientRequestDTO.getStockQuantity() != null) {
            ing.updateStockQuantity(ingredientRequestDTO.getStockQuantity());
        }
        return toResponseDTO(ing);
    }

    //재료 삭제
    @Override
    public void deleteIngredient(Long ingredientId) {
        if(!ingredientRepo.existsById(ingredientId)) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND_PRODUCT_ID
            );
        }
        ingredientRepo.deleteById(ingredientId);
    }

    /** Entity → Response DTO 변환 헬퍼 */
    private IngredientResponseDTO toResponseDTO(Ingredient ing) {
        return IngredientResponseDTO.builder()
                .ingredientId(ing.getIngredientId())
                .name(ing.getName())
                .unit(ing.getUnit())
                .pricePerUnit(ing.getPricePerUnit())
                .description(ing.getDescription())
                .stockQuantity(ing.getStockQuantity())
                .redDate(ing.getRegDate())
                .modDate(ing.getModDate())
                .build();
    }
}
