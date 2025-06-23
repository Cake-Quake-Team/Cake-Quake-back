package com.cakequake.cakequakeback.procurement.service;

import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.procurement.dto.procurement.ConfirmProcurementDTO;
import com.cakequake.cakequakeback.procurement.dto.procurement.ProcurementItemResponseDTO;
import com.cakequake.cakequakeback.procurement.dto.procurement.ProcurementRequestDTO;
import com.cakequake.cakequakeback.procurement.dto.procurement.ProcurementResponseDTO;
import com.cakequake.cakequakeback.procurement.entities.Procurement;
import com.cakequake.cakequakeback.procurement.entities.ProcurementItem;
import com.cakequake.cakequakeback.procurement.entities.ProcurementStatus;
import com.cakequake.cakequakeback.procurement.repo.IngredientRepo;
import com.cakequake.cakequakeback.procurement.repo.ProcurementItemRepository;
import com.cakequake.cakequakeback.procurement.repo.ProcurementRepo;
import com.cakequake.cakequakeback.procurement.validator.ProcurementValidator;
import com.cakequake.cakequakeback.shop.repo.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional
public class ProcurementServiceImpl implements ProcurementService{

    private final ProcurementRepo procurementRepo;
    private final ProcurementItemRepository procurementItemRepository;
    private final ProcurementValidator validator;
    private final ShopRepository shopRepository;
    private final IngredientRepo ingredientRepo;

    //매장별 요청 내역 페이지 조회
    @Override
    @Transactional(readOnly = true)
    public InfiniteScrollResponseDTO<ProcurementResponseDTO> getStoreRequests(PageRequestDTO pageRequestDTO, Long shopId) {
        //매장 존재 여부 검증
        validator.validateShopExists(shopId);

        Pageable pageable = pageRequestDTO.getPageable("procurementId");

        Page<ProcurementResponseDTO> page = procurementRepo.listByShop(shopId, pageable);

        return buildResponseDTO(page);
    }

    //요청 상태별 내역 페이지 조회
    @Override
    @Transactional(readOnly = true)
    public InfiniteScrollResponseDTO<ProcurementResponseDTO> getRequestsByStatus(PageRequestDTO pageRequestDTO, ProcurementStatus status) {
        //상태 검증
        validator.validateStatus(status);

        Pageable pageable = pageRequestDTO.getPageable("procurementId");
        Page<ProcurementResponseDTO> page = procurementRepo.listByStatus(status, pageable);

        return buildResponseDTO(page);
    }


    //매장 및 상태 복합 조건 페이지 조회
    @Override
    @Transactional(readOnly = true)
    public InfiniteScrollResponseDTO<ProcurementResponseDTO> getStoreRequestsByStatus(PageRequestDTO pageRequestDTO, Long shopId, ProcurementStatus status) {
        validator.validateShopExists(shopId);
        validator.validateStatus(status);

        Pageable pageable = pageRequestDTO.getPageable("procurementId");
        Page<ProcurementResponseDTO> page = procurementRepo.listByShopAndStatus(shopId, status, pageable);

        return buildResponseDTO(page);
    }

    //단건 조회
    @Override
    @Transactional(readOnly = true)
    public ProcurementResponseDTO getRequest(Long shopId, Long procurementId) {
        validator.validateShopExists(shopId);
        //요청 존재 여부 조회
        Procurement procurement = validator.findProcurementOrThrow(procurementId);
        //소유권 검증
        validator.validateShopOwner(procurement,shopId);
        //항목 조회
        List<ProcurementItem> items = procurementItemRepository.findByProcurement_ProcurementId(procurementId);

        return toResponseDTO(procurement,items);
    }

    //신규 요청 생성
    @Override
    public ProcurementResponseDTO createProcurement(ProcurementRequestDTO request) {
        //DTO내 shopId필수값 검증
        Long shopId = request.getShopId();
        validator.validateShopExists(shopId);
        validator.validateItems(request.getItems());

        //shop엔티티 조회
        var shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_SHOP_ID));

        //Procurement생성 및 저장
        Procurement procurement = Procurement.builder()
                .shop(shop)
                .note(request.getNote())
                .build();
        Procurement saved = procurementRepo.save(procurement);

        //각 아이템 생성 후 저장
        List<ProcurementItem> items = request.getItems().stream()
                .map(itemDTO -> {
                    // ① Ingredient 엔티티 조회
                    var ingredient = ingredientRepo.findById(itemDTO.getIngredientId())
                            .orElseThrow(() -> new BusinessException(ErrorCode. NOT_FOUND_INGREDIENT_ID));

                    // ② ProcurementItem 생성 (반드시 return)
                    return ProcurementItem.builder()
                            .procurement(saved)
                            .ingredient(ingredient)        // 관계 필드에 엔티티 넣기
                            .quantity(itemDTO.getQuantity())
                            .build();
                })
                .collect(Collectors.toList());

        procurementItemRepository.saveAll(items);

        return toResponseDTO(saved,items);
    }

    //관리자 확정(일정 지정) -> 스케줄 설정, 상태 변경
    @Override
    public ProcurementResponseDTO confirmProcurement(Long procurementId, ConfirmProcurementDTO confirmDTO) {

        //요청 존재 및 상태 유효성 검증
        Procurement procurement = validator.findProcurementOrThrow(procurementId);
        validator.validateConfirm(procurement, confirmDTO.getScheduledDate() );

        //일정 및 상태 업데이트
        procurement.updateScheduledDate(confirmDTO.getScheduledDate());
        procurement.updateStatus(ProcurementStatus.SCHEDULED);

        //변경된 엔티티에 매핑된 항목 조회
        List<ProcurementItem> items = procurementItemRepository.findByProcurement_ProcurementId(procurementId);

        //재고 차감 로직, 알림 발송 등 추가 예정

        return toResponseDTO(procurement,items);
    }

    @Override
    @Transactional(readOnly = true)
    public InfiniteScrollResponseDTO<ProcurementResponseDTO> getAllRequests(PageRequestDTO pageRequestDTO) {
        Pageable pageable = pageRequestDTO.getPageable("procurementId");
        Page<Procurement> page = procurementRepo.findAll(pageable);
        List<ProcurementResponseDTO> dtos = page.stream()
                .map(p->{
                    List<ProcurementItem> items = procurementItemRepository.findByProcurement_ProcurementId(p.getProcurementId());
                    return toResponseDTO(p,items);
                })
                .collect(Collectors.toList());
        Page<ProcurementResponseDTO> dtoPage = new PageImpl<>(dtos,pageable,page.getTotalElements());
        return buildResponseDTO(dtoPage);

    }

    @Override
    @Transactional(readOnly = true)
    public ProcurementResponseDTO getRequestById(Long procurementId) {
        Procurement procurement = validator.findProcurementOrThrow(procurementId);
        List<ProcurementItem> items = procurementItemRepository.findByProcurement_ProcurementId(procurementId);
        return toResponseDTO(procurement,items);
    }


    //Page -> InfiniteScrollResponseDTO 변환
    private InfiniteScrollResponseDTO<ProcurementResponseDTO> buildResponseDTO(Page<ProcurementResponseDTO> page) {
        return InfiniteScrollResponseDTO.<ProcurementResponseDTO>builder()
                .content(page.getContent())
                .hasNext(page.hasNext())
                .totalCount((int) page.getTotalElements())
                .build();
    }

    //엔티티 +아이템 리스트 -> DTO변환
    private ProcurementResponseDTO toResponseDTO(Procurement p, List<ProcurementItem> items) {
        List<ProcurementItemResponseDTO> respItems = items.stream()
                .map(i -> {
                    // relation 필드에서 ID 꺼내기
                    Long ingrId = i.getIngredient().getIngredientId();
                    return ProcurementItemResponseDTO.builder()
                            .itemId(i.getProcurementItemId())
                            .ingredientId(ingrId)
                            .ingredientName(i.getIngredient().getName())
                            .unit(i.getIngredient().getUnit())
                            .quantity(i.getQuantity())
                            .build();
                })
                .collect(Collectors.toList());
        return ProcurementResponseDTO.builder()
                .procurementId(p.getProcurementId())
                .shopId(p.getShop().getShopId())
                .shopName(p.getShop().getShopName())
                .status(p.getStatus())
                .note(p.getNote())
                .scheduleDate(p.getScheduledDate())
                .regDate(p.getRegDate())
                .items(respItems)
                .build();

    }
}
