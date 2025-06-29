package com.cakequake.cakequakeback.review.event;

import org.springframework.context.ApplicationEvent;

public class ReviewChangedEvent  extends ApplicationEvent {

    private final Long shopId;

    public ReviewChangedEvent(Object source, Long shopId) {
        super(source);
        this.shopId = shopId;
    }

    public Long getShopId() {
        return shopId;
    }
}
