package com.cakequake.cakequakeback.shop.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Entity
@Table( name = "shopnotices" )
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ShopNotice {
    @Id
   @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "shopnotice_seq_gen")
   @SequenceGenerator(
           name = "shopnotice_seq_gen",
           sequenceName="shopnotice_seq",
           initialValue=1,
           allocationSize = 50
   )
    private Long shopnoticeId;

    @ManyToOne(fetch =FetchType.LAZY)
    @JoinColumn(name="shopId",nullable = false)
    private Shop shop;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false,columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private boolean isVisible=true;


}
