package com.cakequake.cakequakeback.cake.option.entities;

import com.cakequake.cakequakeback.cake.item.entities.CakeItem;
import com.cakequake.cakequakeback.cake.option.types.CreamType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cream_option")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class Cream {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cream_option_seq")
    @SequenceGenerator(
            name = "cream_option_seq",
            sequenceName = "cream_option_seq",
            initialValue = 1,
            allocationSize = 50
    )
    private Long creamId;

    @Enumerated(EnumType.STRING)
    @Column(name = "creamType", nullable = false)
    private CreamType creamType;    // INNERCREAM, OUTERCREAM

    @Column(name = "flavor", nullable = false)
    private String flavor;           // 생크림, 초코크림, 딸기크림

    @Column(name = "price", nullable = false)
    private int price;

    //@ManyToOne(fetch = FetchType.LAZY)
    //@JoinColumn(name = "shopId", nullable = false)
    // private Shop shop;

}
