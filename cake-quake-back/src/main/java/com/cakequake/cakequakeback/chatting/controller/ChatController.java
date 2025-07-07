package com.cakequake.cakequakeback.chatting.controller;

import com.cakequake.cakequakeback.chatting.dto.ChatMessageDto;
import com.cakequake.cakequakeback.chatting.dto.ChatRoomListDTO;
import com.cakequake.cakequakeback.chatting.dto.ChatRoomRequestDTO;
import com.cakequake.cakequakeback.chatting.entities.ChatRoom;
import com.cakequake.cakequakeback.chatting.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    // ⭐ 추가: 판매자용 채팅방 목록 조회 엔드포인트
    @GetMapping("/seller/rooms/{shopId}")
    public ResponseEntity<List<ChatRoomListDTO>> getSellerChatRooms(@PathVariable Long shopId) {
        List<ChatRoomListDTO> chatRooms = chatService.getChatRoomsForSellerAndShop(shopId);
        return ResponseEntity.ok(chatRooms);
    }

    // ⭐ 추가: 특정 채팅방의 과거 메시지 조회 엔드포인트
    @GetMapping("/rooms/{roomKey}/messages")
    public ResponseEntity<List<ChatMessageDto>> getChatMessages(@PathVariable String roomKey) {
        List<ChatMessageDto> messages = chatService.getChatMessagesByRoomKey(roomKey);
        return ResponseEntity.ok(messages);
    }
}
