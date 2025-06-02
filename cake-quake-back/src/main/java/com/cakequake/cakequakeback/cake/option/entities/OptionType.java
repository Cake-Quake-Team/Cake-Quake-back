package com.cakequake.cakequakeback.cake.option.entities;

import com.cakequake.cakequakeback.cake.option.dto.AddOptionTypeDTO;
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
    @JoinColumn(name = "shopId")
    private Shop shop;

    @Column
    private String optionType;                   // 시트모양, 시트크기, 케이크단 등등

    @Column
    private Boolean isUsed = true;               // 사용여부

    @Column
    private Boolean isDeleted = false;           // 삭제여부

    @Column
    private Boolean isRequired = false;          // 필수선택여부

    @Column
    private Integer minSelection = 0;                // 최소선택개수

    @Column
    private Integer maxSelection = 1;                // 최대선택개수

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createdBy")
    private Member createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modifiedBy")
    private Member modifiedBy;

    public void updateFromDTO(UpdateOptionTypeDTO dto) {
        if (dto.getOptionType() != null) this.optionType = dto.getOptionType();
        if (dto.getIsRequired() != null) this.isRequired = dto.getIsRequired();
        if (dto.getIsUsed() != null) this.isUsed = dto.getIsUsed();
        if (dto.getMinSelection() != null) {
            this.minSelection = dto.getMinSelection();
            if (this.minSelection >= 1) {
                this.isRequired = true;
            }
        }
        if (dto.getMaxSelection() != null) this.maxSelection = dto.getMaxSelection();
    }

    public void changeIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    // 삭제된 옵션타입과 같은 이름의 옵션타입을 등록하려고 할 때
    public void restoreOptionType(AddOptionTypeDTO dto) {
        this.isDeleted = false;
        this.isUsed = true;

        if (dto.getIsRequired() != null) {
            this.isRequired = dto.getIsRequired();
        }
        // 값이 없으면 기존 값 유지
        if (dto.getMinSelection() != null) {
            this.minSelection = dto.getMinSelection();
            if (this.minSelection >= 1) {
                this.isRequired = true;
            }
        }
        if (dto.getMaxSelection() != null) {
            this.maxSelection = dto.getMaxSelection();
        }
    }


}
