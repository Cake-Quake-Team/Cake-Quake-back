package com.cakequake.cakequakeback.chatting.dto;

import lombok.*;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomListDTO {
    private Long id; // ChatRoom의 ID
    private String roomKey;
    private Long buyerUid;
    private String buyerUsername; // 구매자 ID (표시용)
    private Long shopId;
    private String shopName;
}
