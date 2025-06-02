package com.cakequake.cakequakeback.cake.option.entities;

import com.cakequake.cakequakeback.cake.option.dto.AddOptionItemDTO;
import com.cakequake.cakequakeback.cake.option.dto.UpdateOptionItemDTO;
import com.cakequake.cakequakeback.common.entities.BaseEntity;
import com.cakequake.cakequakeback.member.entities.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "option_item", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"optionTypeId", "optionName"}) // optionTypeId와 optionName 조합이 유일
})
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@ToString
public class OptionItem extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long optionItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "optionTypeId")
    private OptionType optionType;

    @Column
    private String optionName;

    @Column
    private int price = 0;

    @Column
    private int version = 1;    // 업데이트 버전 - 옵션 값 수정 시 1씩 증가

    @Column
    private Boolean isDeleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createdBy")
    private Member createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modifiedBy")
    private Member modifiedBy;

    public void updateFromDTO(UpdateOptionItemDTO dto) {
        boolean updated = false;
        if (dto.getOptionName() != null && !dto.getOptionName().equals(this.optionName)) {this.optionName = dto.getOptionName(); updated = true;}
        if (dto.getPrice() != null && dto.getPrice() != this.price) {this.price = dto.getPrice(); updated = true;}
        if (updated) this.version += 1;
    }

    public void changeIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    // 삭제된 옵션타입과 같은 이름의 옵션타입을 등록하려고 할 때
    public void restoreOptionItem(AddOptionItemDTO dto) {
        this.isDeleted = false;

        // 값이 없으면 기존 값 유지
        if (dto.getPrice() != null) this.price = dto.getPrice();

    }
}
