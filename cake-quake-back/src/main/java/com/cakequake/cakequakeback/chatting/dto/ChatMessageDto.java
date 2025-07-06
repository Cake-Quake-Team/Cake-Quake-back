package com.cakequake.cakequakeback.chatting.dto;

import lombok.Data;
import lombok.Getter;

@Data
public class ChatMessageDto {
    private Long senderUid;
    private String message;
}
