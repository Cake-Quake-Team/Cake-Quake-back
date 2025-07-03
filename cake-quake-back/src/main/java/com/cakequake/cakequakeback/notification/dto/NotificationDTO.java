package com.cakequake.cakequakeback.notification.dto;

import com.cakequake.cakequakeback.notification.entities.Notification;
import com.cakequake.cakequakeback.notification.entities.NotificationType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NotificationDTO {

    private final Long id;
    private final Long referenceId;
    private final String content;

    @JsonProperty("isRead")
    private final boolean isRead;
    private final NotificationType type;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private final LocalDateTime regDate;

    public NotificationDTO(Notification noti) {
        this.id = noti.getNotiId();
        this.referenceId = noti.getReferenceId();
        this.content = noti.getContent();
        this.isRead = noti.isRead();
        this.type = noti.getType();
        this.regDate = noti.getRegDate();
    }
}

