package com.cakequake.cakequakeback.chatting.service;

import com.cakequake.cakequakeback.chatting.entities.ChatRoom;
import com.cakequake.cakequakeback.chatting.repo.ChatRoomRepository;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.member.repo.MemberRepository;
import com.cakequake.cakequakeback.shop.entities.Shop;
import com.cakequake.cakequakeback.shop.repo.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor

public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;
    private final ShopRepository shopRepository;

    public ChatRoom createOrFindRoom(Long sellerUid, Long buyerUid, Long shopId) {
        if (sellerUid == null || buyerUid == null || shopId == null) {
            throw new IllegalArgumentException("sellerUid, buyerUid, shopId 중 하나가 null입니다.");
        }

        Member seller = memberRepository.findById(sellerUid)
                .orElseThrow(() -> new IllegalArgumentException("판매자 정보 없음"));

        Member buyer = memberRepository.findById(buyerUid)
                .orElseThrow(() -> new IllegalArgumentException("구매자 정보 없음"));

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("매장 정보 없음"));

        // 기존 채팅방을 찾습니다.
        Optional<ChatRoom> existingRoom = chatRoomRepository.findBySellerAndBuyerAndShop(seller, buyer, shop);

        // 기존 채팅방이 없으면 새로 생성하여 저장합니다.
        return existingRoom.orElseGet(() -> {
            String roomKey = UUID.randomUUID().toString(); // roomKey를 여기서 생성

            ChatRoom newRoom = ChatRoom.builder()
                    .seller(seller)
                    .buyer(buyer)
                    .shop(shop)
                    .roomKey(roomKey)
                    .build();
            return chatRoomRepository.save(newRoom);
        });
    }


    public ChatRoom findRoomByKey(String roomKey) {
        return chatRoomRepository.findByRoomKey(roomKey)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));
    }
}
