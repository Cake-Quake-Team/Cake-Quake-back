package com.cakequake.cakequakeback.procurement.validator;

import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.procurement.dto.ProcurementItemRequestDTO;
import com.cakequake.cakequakeback.procurement.entities.Procurement;
import com.cakequake.cakequakeback.procurement.entities.ProcurementStatus;
import com.cakequake.cakequakeback.procurement.repo.ProcurementRepo;
import com.cakequake.cakequakeback.shop.entities.Shop;
import com.cakequake.cakequakeback.shop.repo.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProcurementValidator {
    private final ShopRepository shopRepository;
    private final ProcurementRepo procurementRepo;

    //매장 존재 여부 검증
    public void validateShopExists(Long shopId) {
        if(!shopRepository.existsById(shopId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_SHOP_ID);
        }
    }

    //요청 상태 유효성 검증
    public void validateStatus(ProcurementStatus status) {
        if(status == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
    }

    //개별 Procurement 조회 또는 예외
    public Procurement findProcurementOrThrow(Long procurementId) {
        return procurementRepo.findById(procurementId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ORDER_ID));
    }

    //요청 소유 매장 일치 여부 검증
    public void validateShopOwner(Procurement procurement, Long shopId) {
        if(!procurement.getShop().getShopId().equals(shopId)) {
            throw new BusinessException(ErrorCode.NO_SHOP_ACCESS,
                    "Shop " + shopId + " does not own procurement " + procurement.getProcurementId());
        }
    }


    //요청 항목 리스트 검증
    public void validateItems(List<ProcurementItemRequestDTO> items){
        if(items == null || items.isEmpty()){
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        for (ProcurementItemRequestDTO item : items) {
            if(item.getIngredientId() == null || item.getIngredientId() <= 0){
                throw new BusinessException(ErrorCode.INVALID_REQUEST,
                        "Invalid ingredientId: " + item.getIngredientId());
            }
            if(item.getQuantity() == null || item.getQuantity() <= 0){
                throw new BusinessException(ErrorCode.INVALID_REQUEST,
                        "Invalid quantity: " + item.getQuantity());
            }
        }
    }

    //관리자 확정 전 상태 및 일정 검증
    public void validateConfirm (Procurement procurement, LocalDateTime scheduledDate) {
        if(scheduledDate == null || scheduledDate.isAfter(LocalDateTime.now())){
            throw new BusinessException(ErrorCode.INVALID_REQUEST,
                    "Scheduled date must be in the future: " + scheduledDate);
        }
        if(procurement.getStatus() != ProcurementStatus.REQUESTED){
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS,
                    "Cannot confirm procurement in status: " + procurement.getStatus());
        }
    }

    //shop엔티티 조회
    public Shop getShop(Long shopId) {
        return shopRepository.findById(shopId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_SHOP_ID));
    }


}
