package com.cakequake.cakequakeback.chatting.service;

import com.cakequake.cakequakeback.chatting.dto.ChatMessageDto;
import com.cakequake.cakequakeback.chatting.dto.ChatRoomListDTO;
import com.cakequake.cakequakeback.chatting.entities.ChatRoom;
import com.cakequake.cakequakeback.chatting.repo.ChatRoomRepository;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.member.repo.MemberRepository;
import com.cakequake.cakequakeback.security.domain.CustomUserDetails;
import com.cakequake.cakequakeback.shop.entities.Shop;
import com.cakequake.cakequakeback.shop.repo.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface ChatService {
    ChatRoom createOrFindRoom(Long sellerUid, Long buyerUid, Long shopId);
    ChatRoom findRoomByKey(String roomKey);
    ChatMessageDto processAndBroadcastChatMessage(String roomKey, ChatMessageDto messageDto);

    //판매자용 채팅방 목록 조회
    List<ChatRoomListDTO> getChatRoomsForSellerAndShop(Long shopId);

    //특정 채팅방의 과거 메시지 조회
    List<ChatMessageDto> getChatMessagesByRoomKey(String roomKey);
}
