package com.cakequake.cakequakeback.cake.item.service;

import com.cakequake.cakequakeback.cake.item.dto.CakeDetailDTO;
import com.cakequake.cakequakeback.cake.item.dto.ImageDTO;
import com.cakequake.cakequakeback.cake.item.dto.MappingRequestDTO;
import com.cakequake.cakequakeback.cake.item.dto.MappingResponseDTO;
import com.cakequake.cakequakeback.cake.item.entities.CakeItem;
import com.cakequake.cakequakeback.cake.item.entities.CakeOptionMapping;
import com.cakequake.cakequakeback.cake.item.repo.MappingRepository;
import com.cakequake.cakequakeback.cake.option.dto.CakeOptionItemDTO;
import com.cakequake.cakequakeback.cake.option.entities.OptionItem;
import com.cakequake.cakequakeback.cake.option.repo.OptionItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MappingServiceImpl implements MappingService {
    private final MappingRepository mappingRepository;
    private final OptionItemRepository optionItemRepository;
    private final CakeImageService cakeImageService;

    @Override
    public MappingResponseDTO saveCakeOptionMapping(MappingRequestDTO requestDTO, CakeItem cakeItem, Long cakeId, Long shopId) {
        List<OptionItem> optionItems = optionItemRepository.findAllById(requestDTO.getOptionItemIds());

        List<CakeOptionMapping> mappings = new ArrayList<>();
        for (OptionItem optionItem : optionItems) {
            CakeOptionMapping mapping = CakeOptionMapping.builder()
                    .cakeItem(cakeItem)
                    .optionItem(optionItem)
                    .isUsed(true)
                    .build();
            mappings.add(mapping);
        }

        mappingRepository.saveAll(mappings);

        // DTO 변환
        List<CakeOptionItemDTO> optionDTOs = optionItems.stream()
                .map(CakeOptionItemDTO::fromEntity)
                .collect(Collectors.toList());

        List<ImageDTO> imageDTOs = cakeImageService.findByCakeItem(cakeItem, cakeId);
        CakeDetailDTO cakeDetailDTO = CakeDetailDTO.from(cakeItem, imageDTOs);

        return MappingResponseDTO.builder()
                .cakeDetailDTO(cakeDetailDTO)
                .options(optionDTOs)
                .build();
    }


    @Override
    // 상품-옵션 매핑 조회
    public List<CakeOptionMapping> getMappings(Long cakeId) {
        return mappingRepository.findByCakeItemCakeId(cakeId);
    }

    @Override
    // 상품-옵션 매핑 수정
    public void updateCakeOptionMappings(CakeItem cakeItem, List<Long> updatedOptionItemIds) {

        if (updatedOptionItemIds == null) {
            // 수정할 옵션이 없으면 아무 것도 변경하지 않음
            return;
        }

        List<CakeOptionMapping> existingMappings = mappingRepository.findByCakeItem(cakeItem);

        // 기존 매핑 → 사용 여부 업데이트
        for (CakeOptionMapping mapping : existingMappings) {
            Long optionId = mapping.getOptionItem().getOptionItemId();
            boolean shouldBeUsed = updatedOptionItemIds.contains(optionId); // 체크된 옵션이면 true
            mapping.changeIsUsed(shouldBeUsed);
        }

        // 새 매핑 추가
        for (Long newId : updatedOptionItemIds) {
            boolean alreadyExists = false;
            for (CakeOptionMapping mapping : existingMappings) {
                if (mapping.getOptionItem().getOptionItemId().equals(newId)) {
                    alreadyExists = true;
                    break;
                }
            }
            if (!alreadyExists) {
                OptionItem optionItem = optionItemRepository.findById(newId)
                        .orElseThrow(() -> new IllegalArgumentException("옵션 없음: " + newId));

                CakeOptionMapping newMapping = CakeOptionMapping.builder()
                        .cakeItem(cakeItem)
                        .optionItem(optionItem)
                        .isUsed(true)
                        .build();

                mappingRepository.save(newMapping);
            }
        }
    }

}
