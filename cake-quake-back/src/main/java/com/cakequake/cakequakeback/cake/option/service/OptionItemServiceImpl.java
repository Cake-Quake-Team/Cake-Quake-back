package com.cakequake.cakequakeback.cake.option.service;

import com.cakequake.cakequakeback.cake.option.dto.*;
import com.cakequake.cakequakeback.cake.option.entities.OptionItem;
import com.cakequake.cakequakeback.cake.option.entities.OptionType;
import com.cakequake.cakequakeback.cake.option.repo.OptionItemRepository;
import com.cakequake.cakequakeback.cake.option.repo.OptionTypeRepository;
import com.cakequake.cakequakeback.cake.validator.OptionValidator;
import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.shop.repo.ShopRepository;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
@ToString
public class OptionItemServiceImpl implements OptionItemService {

    private final OptionItemRepository optionItemRepository;
    private final ShopRepository shopRepository;
    private final OptionTypeRepository optionTypeRepository;
    private final OptionValidator optionValidator;

    @Override
    // 옵션 값 등록
    public Long addOptionItem(Long shopId, AddOptionItemDTO addOptionItemDTO) {

        optionValidator.validateShop(shopId);
        OptionType optionType = optionValidator.validateOptionType(addOptionItemDTO.getOptionTypeId());
        optionValidator.validateAddOptionItem(addOptionItemDTO);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        Member member = optionValidator.validateMember(userId);

        // 삭제된 동일 이름의 OptionName이 존재하는지 확인
        Optional<OptionItem> alreadyDeleted = optionItemRepository.findByOptionNameAndIsDeletedTrue(addOptionItemDTO.getOptionName());

        if (alreadyDeleted.isPresent()) {
            OptionItem deleted = alreadyDeleted.get();
            deleted.restoreOptionItem(addOptionItemDTO);

            return deleted.getOptionItemId();
        }

        OptionItem optionItem = OptionItem.builder()
                .optionType(optionType)
                .optionName(addOptionItemDTO.getOptionName())
                .price(addOptionItemDTO.getPrice())
                .version(1)
                .isDeleted(false)
                .createdBy(member)
                .build();

        optionItemRepository.save(optionItem);

        log.info("옵션 값이 등록되었습니다. 옵션명: " + optionItem.getOptionName());

        return optionItem.getOptionItemId();
    }

    @Override
    @Transactional(readOnly = true)
    // 옵션 값 목록 조회
    public InfiniteScrollResponseDTO<CakeOptionItemDTO> getOptionItemList(Long shopId, PageRequestDTO pageRequestDTO) {

        optionValidator.validateShop(shopId);
        optionValidator.validatePaging(pageRequestDTO);

        Pageable pageable = pageRequestDTO.getPageable("regDate");

        Page<OptionItem> itemListPage = optionItemRepository.findOptionItem(shopId, pageable);

        List<CakeOptionItemDTO> dtoList = itemListPage.getContent().stream()
                .map(CakeOptionItemDTO::fromEntity)
                .toList();

        return InfiniteScrollResponseDTO.<CakeOptionItemDTO>builder()
                .content(dtoList)
                .hasNext(itemListPage.hasNext())
                .totalCount((int) itemListPage.getTotalElements())
                .build();
    }

    @Override
    // 옵션 값 상세 조회
    public OptionItemDetailDTO getOptionItemDetail(Long shopId, Long optionItemId) {

        optionValidator.validateShop(shopId);
        OptionItem optionItem = optionValidator.vlidateOptionItem(optionItemId);

        return OptionItemDetailDTO.builder()
                .optionItemId(optionItem.getOptionItemId())
                .optionName(optionItem.getOptionName())
                .price(optionItem.getPrice())
                .isDeleted(optionItem.getIsDeleted())
                .regDate(optionItem.getRegDate())
                .modDate(optionItem.getModDate())
                .build();
    }

    @Override
    // 옵션 값 수정
    public void updateOptionItem(Long shopId, Long optionItemId, UpdateOptionItemDTO updateOptionItemDTO) {

        optionValidator.validateShop(shopId);
        OptionItem optionItem = optionValidator.vlidateOptionItem(optionItemId); // 기존 옵션 항목 조회

        Member member = optionValidator.validateMember(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );

        // 원본과 값이 모두 동일하면 패스 (이 로직은 새로운 버전을 생성하는 경우에도 유효성을 검사합니다.)
        boolean noChange =
                (updateOptionItemDTO.getOptionName() == null || updateOptionItemDTO.getOptionName().equals(optionItem.getOptionName())) &&
                        (updateOptionItemDTO.getPrice() == null || updateOptionItemDTO.getPrice() == optionItem.getPrice());

        // DTO에서 변경하려는 '이름'과 '가격'이 모두 null이거나 기존 값과 동일하다면,
        // 새로운 버전을 생성할 필요가 없으므로 여기서 종료합니다.
        if (noChange) return;

        // 3. 기존 항목은 삭제 처리 (Soft Delete)
        optionItem.changeIsDeleted(true);
        // ★★★ 기존 항목의 변경사항을 DB에 명시적으로 반영합니다. ★★★
        optionItemRepository.save(optionItem); // 이렇게 호출하면 기존 항목의 isDeleted 변경이 즉시 반영됩니다.

        // 4. 새로운 버전 생성
        OptionItem newItem = OptionItem.builder()
                .optionType(optionItem.getOptionType())
                .optionName(updateOptionItemDTO.getOptionName() != null ? updateOptionItemDTO.getOptionName() : optionItem.getOptionName())
                .price(updateOptionItemDTO.getPrice() != null ? updateOptionItemDTO.getPrice() : optionItem.getPrice())
                .version(optionItem.getVersion() + 1) // 기존 버전 + 1
                .isDeleted(false) // 새로운 항목이므로 false
                .createdBy(optionItem.getCreatedBy())
                .modifiedBy(member)
                .build();

        optionItemRepository.save(newItem); // 새 항목 저장 (INSERT)
    }

    @Override
    // 옵션 값 삭제
    public void deleteOptionItem(Long shopId, Long optionItemId) {

        optionValidator.validateShop(shopId);
        OptionItem optionItem = optionValidator.vlidateOptionItem(optionItemId);

        optionItem.changeIsDeleted(true);
    }
}
