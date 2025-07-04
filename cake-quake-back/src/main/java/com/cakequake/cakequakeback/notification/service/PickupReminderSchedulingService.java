package com.cakequake.cakequakeback.notification.service;

import com.cakequake.cakequakeback.order.entities.CakeOrder;

public interface PickupReminderSchedulingService {

    void schedulePickupReminder(CakeOrder order);

    void processScheduledNotifications();
}
