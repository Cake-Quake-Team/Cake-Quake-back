package com.cakequake.cakequakeback.chatting.dto;

import lombok.Data;

@Data
public class ChatRoomRequestDTO {
    private Long sellerUid;
    private Long buyerUid;
    private Long shopId;
}
