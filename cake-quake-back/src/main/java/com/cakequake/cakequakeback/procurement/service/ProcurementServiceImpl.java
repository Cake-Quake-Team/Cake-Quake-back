package com.cakequake.cakequakeback.procurement.service;

import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.procurement.config.ProcurementProperties;
import com.cakequake.cakequakeback.procurement.dto.procurement.*;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
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
    private final ProcurementProperties props;

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

    //예상 배송일 계산
    private LocalDate calculateEstimatedArrivalDate(){
        LocalDate today = LocalDate.now();

        int currentHour = LocalTime.now().getHour();

        LocalDate processingDate = today;
        if(currentHour > props.getCutoffHour()){
            processingDate = processingDate.plusDays(1);
        }

        return processingDate.plusDays(props.getTransitDays());
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


        // 3) ETA 계산
        LocalDate eta = calculateEstimatedArrivalDate();

        //Procurement생성 및 저장
        Procurement procurement = Procurement.builder()
                .shop(shop)
                .note(request.getNote())
                .estimatedArrivalDate(eta)    // ★ 여기에 ETA 세팅
                .build();
        Procurement saved = procurementRepo.save(procurement);

        //각 아이템 생성 후 저장
        List<ProcurementItem> items = request.getItems().stream()
                .map(itemDTO -> {
                    // ① Ingredient 엔티티 조회
                    var ingredient = ingredientRepo.findById(itemDTO.getIngredientId())
                            .orElseThrow(() -> new BusinessException(ErrorCode. NOT_FOUND_INGREDIENT_ID));

                    // 3-2) 실시간 재고 차감 (반환값 0 이면 재고 부족)
                    int updated = ingredientRepo.adjustStock(ingredient.getIngredientId(), -itemDTO.getQuantity());
                    if (updated == 0) {
                        throw new BusinessException(
                                ErrorCode.NOT_ENOUGH_STOCK,
                                ErrorCode.NOT_ENOUGH_STOCK.getMessage()
                        );
                    }
                    // ② ProcurementItem 생성 (반드시 return)
                    return ProcurementItem.builder()
                            .procurement(saved)
                            .ingredient(ingredient)        // 관계 필드에 엔티티 넣기
                            .quantity(itemDTO.getQuantity())
                            .ingredientName(ingredient.getName())
                            .unitPrice(ingredient.getPricePerUnit().intValue())
                            .build();
                })
                .collect(Collectors.toList());

        procurementItemRepository.saveAll(items);

        procurement.updateStatus(ProcurementStatus.COMPLETED);

        return toResponseDTO(saved,items);
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

    @Override
    public ProcurementResponseDTO cancelProcurement(Procurement procurement, CancelProcurementDTO cancelDTO) {

        if(procurement.getStatus().compareTo(ProcurementStatus.SHIPPED) >=0 ){
            throw new BusinessException(
                    ErrorCode.INVALID_ORDER_STATUS
            );
        }

        procurement.cancel(cancelDTO.getReason());

        List<ProcurementItem> items = procurementItemRepository.findByProcurement_ProcurementId(procurement.getProcurementId());

        return toResponseDTO(procurement,items);
    }

    @Override
    public ProcurementResponseDTO cancelBySeller(Long shopId, Long procurementId, CancelProcurementDTO cancelDTO) {
        validator.validateShopExists(shopId);
        Procurement procurement = validator.findProcurementOrThrow(procurementId);
        validator.validateShopOwner(procurement,shopId);


        return cancelProcurement(procurement, cancelDTO);
    }

    @Override
    public ProcurementResponseDTO cancelByAdmin(Long procurementId, CancelProcurementDTO cancelDTO) {
       Procurement procurement =validator.findProcurementOrThrow(procurementId);

        return cancelProcurement(procurement, cancelDTO);
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
                            .ingredientName(i.getIngredientName())
                            .unitPrice(i.getUnitPrice())
                            .quantity(i.getQuantity())
                            .build();
                })
                .collect(Collectors.toList());

         BigDecimal total = items.stream()
                         .map(i -> BigDecimal.valueOf(i.getUnitPrice())
                                .multiply(BigDecimal.valueOf(i.getQuantity())))
                         .reduce(BigDecimal.ZERO, BigDecimal::add);

        return ProcurementResponseDTO.builder()
                .procurementId(p.getProcurementId())
                .shopId(p.getShop().getShopId())
                .shopName(p.getShop().getShopName())
                .status(p.getStatus())
                .note(p.getNote())
                .estimatedArrivalDate(p.getEstimatedArrivalDate())
                .regDate(p.getRegDate())
                .cancelReason(p.getCancelReason())
                .items(respItems)
                .totalPrice(total)
                .build();

    }
}
