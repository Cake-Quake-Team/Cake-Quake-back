package com.cakequake.cakequakeback.chatting.controller;

import com.cakequake.cakequakeback.chatting.dto.ChatMessageDto;
import com.cakequake.cakequakeback.chatting.service.ChatService;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.security.domain.CustomUserDetails;
import com.cakequake.cakequakeback.security.service.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatService chatService;
    private final AuthenticatedUserService authenticatedUserService;


    @MessageMapping("/chat/{roomKey}")

    public void sendChatMessage(@DestinationVariable String roomKey, ChatMessageDto messageDto) {
        // 👉 현재 로그인한 사용자 정보 가져오기 (SecurityContextHolder 접근 X)
        Member currentMember = authenticatedUserService.getCurrentMember();
        Long senderUid = currentMember.getUid();
        String senderUsername = currentMember.getUserId();  // Member에 username 필드가 있다고 가정

        // ❗ 프론트에서 보낸 sender 정보 무시하고 강제 세팅
        messageDto.setSenderUid(senderUid);
        messageDto.setSenderUsername(senderUsername);

        log.info("ChatMessageController: 메시지 수신 요청 - roomKey={}, sender={}", roomKey, senderUid);

        chatService.processAndBroadcastChatMessage(roomKey, messageDto);

        log.info("ChatMessageController: 메시지 처리 요청 완료 (서비스로 위임)");
    }

}

