package com.cakequake.cakequakeback.chatting.service;

import com.cakequake.cakequakeback.chatting.dto.ChatMessageDto;
import com.cakequake.cakequakeback.chatting.entities.ChatMessage;
import com.cakequake.cakequakeback.chatting.entities.ChatRoom;
import com.cakequake.cakequakeback.chatting.repo.ChatMessageRepository;
import com.cakequake.cakequakeback.chatting.repo.ChatRoomRepository;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.member.repo.MemberRepository;
import com.cakequake.cakequakeback.security.service.AuthenticatedUserService;
import com.cakequake.cakequakeback.shop.entities.Shop;
import com.cakequake.cakequakeback.shop.repo.ShopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j

public class ChatServiceImpl implements ChatService {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;
    private final ShopRepository shopRepository;
    private final AuthenticatedUserService authenticatedUserService;

    @Override
    @Transactional
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

@Override
    public ChatRoom findRoomByKey(String roomKey) {
        return chatRoomRepository.findByRoomKey(roomKey)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));
    }

    @Override
    public ChatMessageDto processAndBroadcastChatMessage(String roomKey, ChatMessageDto messageDto) {
        log.debug("ChatService: 메시지 처리 시작 - roomKey={}, senderId={}", roomKey, messageDto.getSenderUid()); // senderId는 messageDto에서 가져옴

        // 1. 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findByRoomKey(roomKey)
                .orElseThrow(() -> {
                    log.warn("ChatService: 존재하지 않는 채팅방입니다. roomKey={}", roomKey);
                    return new IllegalArgumentException("채팅방을 찾을 수 없습니다: " + roomKey);
                });

        // 2. 발신자 정보 (AuthenticatedUserService 사용)
        Member sender = authenticatedUserService.getCurrentMember(); // ⭐ AuthenticatedUserService 사용
        if (sender == null) { // AuthenticatedUserService 내부에서 예외 처리하므로 이 if문은 불필요할 수 있으나, 방어적으로 남겨둠
            log.error("ChatService: 현재 인증된 사용자(Member) 정보를 가져올 수 없습니다.");
            throw new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다.");
        }
        log.debug("ChatService: 발신자 확인 - UID={}, Username={}", sender.getUid(), sender.getUserId());

        // 3. 메시지 엔티티 생성 및 저장
        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .message(messageDto.getMessage())
                .build();
        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
        log.debug("ChatService: 메시지 저장 완료 - messageId={}", savedMessage.getId());

        // 4. 클라이언트에게 브로드캐스트할 DTO 구성 (Builder 패턴 사용)
        ChatMessageDto responseDto = ChatMessageDto.builder()
                .roomId(roomKey)
                .senderUid(sender.getUid())
                .senderUsername(sender.getUserId())
                .message(savedMessage.getMessage())
                .timestamp(savedMessage.getRegDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .build();
        log.debug("ChatService: 브로드캐스트 DTO 구성 완료 - {}", responseDto);

        // 5. 메시지 브로드캐스트 (클라이언트 구독 경로: /topic/chat/room/{roomKey})
        String destination = "/topic/chat/room/" + roomKey;
        messagingTemplate.convertAndSend(destination, responseDto);
        log.info("ChatService: 메시지 브로드캐스트 완료 - Destination={}, Message={}", destination, responseDto);

        return responseDto;
    }

}
