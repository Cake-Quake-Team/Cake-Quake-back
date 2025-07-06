package com.cakequake.cakequakeback.chatting.controller;

import com.cakequake.cakequakeback.chatting.dto.ChatMessageDto;
import com.cakequake.cakequakeback.chatting.entities.ChatMessage;
import com.cakequake.cakequakeback.chatting.entities.ChatRoom;
import com.cakequake.cakequakeback.chatting.repo.ChatMessageRepository;
import com.cakequake.cakequakeback.chatting.repo.ChatRoomRepository;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.member.repo.MemberRepository;
import com.cakequake.cakequakeback.security.domain.CustomUserDetails;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;
    private final ChatMessageRepository chatMessageRepository;

    @MessageMapping("/chat/{roomKey}")
    // ⭐ @AuthenticationPrincipal을 사용하여 현재 인증된 사용자 정보에 접근
    public void sendChatMessage(@DestinationVariable String roomKey, ChatMessageDto messageDto,
                                @AuthenticationPrincipal CustomUserDetails userDetails) { // 현재 로그인된 사용자 정보

        // userDetails를 통해 로그인된 사용자의 UID를 직접 가져올 수 있습니다.
        // 이로써 messageDto.getSenderUid()의 유효성을 다시 한번 검증하거나,
        // messageDto에서 senderUid를 제거하고 여기서 직접 사용하는 것도 가능합니다.
        log.info("메시지 수신: roomKey={}, sender={}, content={}", roomKey, userDetails.getUsername(), messageDto.getMessage());

        // 1. 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findByRoomKey(roomKey)
                .orElseThrow(() -> new IllegalArgumentException("채팅방 없음: " + roomKey));

        // 2. 발신자 조회 (이미 인증된 사용자이므로 memberRepository에서 다시 조회할 필요 없음, userDetails에서 직접 사용)
        Member sender = userDetails.getMember(); // CustomUserDetails에 Member 객체 접근 메서드 추가 필요

        // 3. 메시지 생성 및 저장
        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender) // userDetails에서 가져온 sender 사용
                .message(messageDto.getMessage())
                .build();
        chatMessageRepository.save(chatMessage);

        // 4. 메시지 전송 (클라이언트에게 반환할 DTO 구성)
        // DTO에 senderName 등 추가 정보가 필요할 경우 ChatMessageDto를 확장하거나 새로 생성합니다.
        ChatMessageDto responseDto = new ChatMessageDto();
        responseDto.setSenderUid(sender.getUid()); // 또는 sender.getUserId() 등
        responseDto.setMessage(chatMessage.getMessage());
        // 필요하다면 메시지 ID, 타임스탬프 등 추가
        // responseDto.setTimestamp(System.currentTimeMillis());

        messagingTemplate.convertAndSend("/topic/chat/" + roomKey, responseDto);
        log.info("메시지 전송 완료: roomKey={}, sender={}, content={}", roomKey, sender.getUserId(), messageDto.getMessage());
    }


}

