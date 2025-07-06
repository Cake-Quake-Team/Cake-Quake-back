package com.cakequake.cakequakeback.chatting.controller;

import com.cakequake.cakequakeback.chatting.dto.ChatRoomRequestDTO;
import com.cakequake.cakequakeback.chatting.entities.ChatRoom;
import com.cakequake.cakequakeback.chatting.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/chatting")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @PostMapping("/rooms")
    public ResponseEntity<Map<String, String>> createOrGetChatRoom(@RequestBody ChatRoomRequestDTO requestDTO) {
        if (requestDTO.getSellerUid() == null || requestDTO.getBuyerUid() == null || requestDTO.getShopId() == null) {
            throw new IllegalArgumentException("필수 파라미터(sellerUid, buyerUid, shopId)가 누락되었습니다.");
        }

        ChatRoom chatRoom = chatService.createOrFindRoom(requestDTO.getSellerUid(), requestDTO.getBuyerUid(), requestDTO.getShopId());
        Map<String, String> response = Map.of("roomKey", chatRoom.getRoomKey());

        return ResponseEntity.ok(response);
    }
}
