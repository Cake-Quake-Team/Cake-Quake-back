package com.cakequake.cakequakeback.cake.option.entities;

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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cake_option_item_seq")
    @SequenceGenerator(
            name = "cake_option_item_seq",
            sequenceName = "cake_option_item_seq",
            initialValue = 1,
            allocationSize = 50
    )
    private Long optionItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "optionTypeId", nullable = false)
    private OptionType optionType;

    @Column(nullable = false)
    private String optionName;

    @Column(nullable = false)
    private int price = 0;

    @Column(nullable = false)
    private Boolean isUsed = true;              // 사용여부

    @Column(nullable = false)
    private int position = 0;                   // 표시순서

    @Column(nullable = false)
    private Boolean allowQuantity = false;      // 수량조절가능여부

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createdBy", nullable = false)
    private Member createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modifiedBy", nullable = false)
    private Member modifiedBy;

}
