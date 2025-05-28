package com.cakequake.cakequakeback.cake.option.entities;

import com.cakequake.cakequakeback.cake.option.dto.UpdateOptionTypeDTO;
import com.cakequake.cakequakeback.common.entities.BaseEntity;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.shop.entities.Shop;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "option_type", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"shopId", "optionType"}) // shopId와 optionType 조합이 유일
})
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Getter
public class OptionType extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long optionTypeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shopId", nullable = false)
    private Shop shop;

    @Column(nullable = false)
    private String optionType;                   // 시트모양, 시트크기, 케이크단 등등

    @Column(nullable = false)
    private Boolean isUsed = true;               // 사용여부

    @Column(nullable = false)
    private Boolean isRequired = false;          // 필수선택여부

    @Column(nullable = false)
    private int minSelection = 0;                // 최소선택개수

    @Column(nullable = false)
    private int maxSelection = 1;                // 최대선택개수

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createdBy", nullable = false)
    private Member createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modifiedBy", nullable = false)
    private Member modifiedBy;

    public void updateFromDTO(UpdateOptionTypeDTO dto) {
        if (dto.getOptionType() != null) this.optionType = dto.getOptionType();
        if (dto.getIsRequired() != null) this.isRequired = dto.getIsRequired();
        if (dto.getIsUsed() != null) this.isUsed = dto.getIsUsed();
        if (dto.getMinSelection() != null) this.minSelection = dto.getMinSelection();
        if (dto.getMaxSelection() != null) this.maxSelection = dto.getMaxSelection();
    }
}
