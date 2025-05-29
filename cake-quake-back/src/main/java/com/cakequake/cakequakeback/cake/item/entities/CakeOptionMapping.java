package com.cakequake.cakequakeback.cake.item.entities;

import com.cakequake.cakequakeback.cake.option.entities.OptionType;
import com.cakequake.cakequakeback.common.entities.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "cake_mapping")
public class CakeOptionMapping extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mappingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cakeId")
    private CakeItem cakeItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "optionTypeId")
    private OptionType optionType;
}
