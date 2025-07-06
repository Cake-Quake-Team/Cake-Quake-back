package com.cakequake.cakequakeback.chatting.entities;

import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.shop.entities.Shop;
import jakarta.persistence.*;
import lombok.*;

@Builder
@Entity
@Table( name = "chatroom")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 판매자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_uid", nullable = false)
    private Member seller;

    // 구매자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_uid", nullable = false)
    private Member buyer;

    // 매장
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Column(nullable = false, unique = true)
    private String roomKey;

}
