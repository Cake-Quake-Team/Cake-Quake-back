package com.cakequake.cakequakeback.chatting.dto;

import lombok.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDto {
    private String roomId;
    private Long senderUid;
    private String message;
    private String senderUsername; // 발신자 사용자 ID (예: "buyer100")
    private Long timestamp;      // 메시지 전송 시간 (Epoch Milliseconds)
    private Long messageId;
}
