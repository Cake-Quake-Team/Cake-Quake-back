package com.cakequake.cakequakeback.cake.option.entities;

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

    @Builder.Default
    @Column(name = "price", nullable = false)
    private int price = 0;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isUsed = true;

    //@ManyToOne(fetch = FetchType.LAZY)
    //@JoinColumn(name = "shopId", nullable = false)
    // private Shop shop;
}
