package com.cakequake.cakequakeback.cake.option.entities;

import com.cakequake.cakequakeback.cake.item.entities.CakeItem;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "extra_option")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class Extra {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "extra_option_seq")
    @SequenceGenerator(
            name = "extra_option_seq",
            sequenceName = "extra_option_seq",
            initialValue = 1,
            allocationSize = 50
    )
    private Long extraId;

    @Column(name = "name", nullable = false)
    private String name;                     // 기본 초, 하트 초, 꽃 토퍼, 생일 축하 토퍼

    @Column(name = "price", nullable = false)
    private int price;

    //@ManyToOne(fetch = FetchType.LAZY)
    //@JoinColumn(name = "shopId", nullable = false)
    // private Shop shop;
}
