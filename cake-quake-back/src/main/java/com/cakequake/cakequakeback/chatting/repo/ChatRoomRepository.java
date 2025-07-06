package com.cakequake.cakequakeback.chatting.repo;

import com.cakequake.cakequakeback.chatting.entities.ChatRoom;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.shop.entities.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findBySellerAndBuyerAndShop(Member seller, Member buyer, Shop shop);
    Optional<ChatRoom> findByRoomKey(String roomKey);
}
