package com.cakequake.cakequakeback.cake.item.entities;

import com.cakequake.cakequakeback.cake.option.entities.OptionType;
import jakarta.persistence.*;

@Entity
@Table(name = "cake_mapping")
public class CakeOptionMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cake_mapping_seq")
    @SequenceGenerator(
            name = "cake_mapping",
            sequenceName = "cake_mapping",
            initialValue = 1,
            allocationSize = 50
    )
    private Long mappingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cakeId", nullable = false)
    private CakeItem cakeItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "optionTypeId", nullable = false)
    private OptionType optionType;
}
