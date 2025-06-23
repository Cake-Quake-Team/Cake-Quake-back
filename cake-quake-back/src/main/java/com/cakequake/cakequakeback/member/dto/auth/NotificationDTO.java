package com.cakequake.cakequakeback.member.dto.auth;

import com.cakequake.cakequakeback.member.entities.Notification;
import com.cakequake.cakequakeback.member.entities.NotificationType;
import lombok.Getter;

@Getter
public class NotificationDTO {

    private final Long id;
    private final String content;
    private final boolean isRead;
    private final NotificationType type;

    public NotificationDTO(Notification noti) {
        this.id = noti.getNotiId();
        this.content = noti.getContent();
        this.isRead = noti.isRead();
        this.type = noti.getType();
    }
}

