package com.cakequake.cakequakeback.chatting.repo;

import com.cakequake.cakequakeback.chatting.entities.ChatMessage;
import com.cakequake.cakequakeback.chatting.entities.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatRoomOrderByRegDateAsc(ChatRoom chatRoom);
}
