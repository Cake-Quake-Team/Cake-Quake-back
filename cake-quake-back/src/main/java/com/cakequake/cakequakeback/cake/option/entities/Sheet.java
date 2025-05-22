package com.cakequake.cakequakeback.cake.option.entities;

import com.cakequake.cakequakeback.cake.item.entities.CakeItem;
import com.cakequake.cakequakeback.cake.option.types.SheetType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sheet_option")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class Sheet {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sheet_option_seq")
    @SequenceGenerator(
            name = "sheet_option_seq",
            sequenceName = "sheet_option_seq",
            initialValue = 1,
            allocationSize = 50
    )
    private Long sheetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "sheetType", nullable = false)
    private SheetType type;     // SHAPE, SIZE, LAYER, FLAVOR

    @Column(name = "value", nullable = false)
    private String value;       // 예 : 하트, 1호, 2단 등

    @Column(name = "price", nullable = false)
    private int price;

    @Column(name = "letteringLimit", nullable = false)
    private int letteringLimit;

    //@ManyToOne(fetch = FetchType.LAZY)
    //@JoinColumn(name = "shopId", nullable = false)
    // private Shop shop;
}
