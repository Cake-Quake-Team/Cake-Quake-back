package com.cakequake.cakequakeback.chatting.controller;

import com.cakequake.cakequakeback.chatting.dto.ChatMessageDto;
import com.cakequake.cakequakeback.chatting.service.ChatService;
import com.cakequake.cakequakeback.security.domain.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatService chatService;


    @MessageMapping("/chat/{roomKey}")

    public void sendChatMessage(@DestinationVariable String roomKey, ChatMessageDto messageDto) {
        // userDetails 대신 messageDto에서 보낸 사람의 UID를 로그에 사용
        log.info("ChatMessageController: 메시지 수신 요청 - roomKey={}, sender={}", roomKey, messageDto.getSenderUid());

        // ⭐ chatService 호출 시 userDetails 매개변수 제거
        chatService.processAndBroadcastChatMessage(roomKey, messageDto);

        log.info("ChatMessageController: 메시지 처리 요청 완료 (서비스로 위임)");
    }

}

