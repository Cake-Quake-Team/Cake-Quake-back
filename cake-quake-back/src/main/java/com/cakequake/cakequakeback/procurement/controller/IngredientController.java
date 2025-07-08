package com.cakequake.cakequakeback.procurement.controller;


import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.procurement.dto.ingredient.IngredientRequestDTO;
import com.cakequake.cakequakeback.procurement.dto.ingredient.IngredientResponseDTO;
import com.cakequake.cakequakeback.procurement.service.IngredientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ingredients")
@RequiredArgsConstructor
public class IngredientController {

    private final IngredientService ingredientService;

    /**
     * 전체 재료 목록 조회 (무한 스크롤)
     * GET /api/ingredients?page={page}&size={size}&sortField={field}
     */
    @GetMapping
    public ResponseEntity<InfiniteScrollResponseDTO<IngredientResponseDTO>> list(
            @Valid PageRequestDTO pageRequestDTO
    ) {
        InfiniteScrollResponseDTO<IngredientResponseDTO> result =
                ingredientService.getAllIngredients(pageRequestDTO);
        return ResponseEntity.ok(result);
    }

    /**
     * 단건 재료 조회
     * GET /api/ingredients/{id}
     */
    @GetMapping("/{ingredientId}")
    public ResponseEntity<IngredientResponseDTO> getOne(
            @PathVariable("ingredientId") Long ingredientId
    ) {
        IngredientResponseDTO dto = ingredientService.getIngredient(ingredientId);
        return ResponseEntity.ok(dto);
    }

    /**
     * 신규 재료 등록
     * POST /api/ingredients
     */
    @PostMapping
    public ResponseEntity<IngredientResponseDTO> create(
            @RequestBody @Valid IngredientRequestDTO request
    ) {
        IngredientResponseDTO dto = ingredientService.createIngredient(request);
        return ResponseEntity.ok(dto);
    }

    /**
     * 재료 정보 수정
     * PUT /api/ingredients/{id}
     */
    @PatchMapping("/{ingredientId}")
    public ResponseEntity<IngredientResponseDTO> update(
            @PathVariable("ingredientId") Long ingredientId,
            @RequestBody @Valid IngredientRequestDTO request
    ) {
        IngredientResponseDTO dto = ingredientService.updateIngredient(ingredientId, request);
        return ResponseEntity.ok(dto);
    }

    /**
     * 재료 삭제
     * DELETE /api/ingredients/{id}
     */
    @DeleteMapping("/{ingredientId}")
    public ResponseEntity<Void> delete(
            @PathVariable("ingredientId") Long ingredientId
    ) {
        ingredientService.deleteIngredient(ingredientId);
        return ResponseEntity.noContent().build();
    }
}