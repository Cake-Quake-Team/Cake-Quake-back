package com.cakequake.cakequakeback.chatting.repo;

import com.cakequake.cakequakeback.chatting.entities.ChatMessage;
import com.cakequake.cakequakeback.chatting.entities.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    // ⭐ 추가: 특정 ChatRoom의 모든 메시지를 등록일(regDate) 오름차순으로 조회
    List<ChatMessage> findByChatRoomOrderByRegDateAsc(ChatRoom chatRoom);
}
