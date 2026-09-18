package com.fundflow.service;

import com.fundflow.dto.notification.NotificationResponse;
import com.fundflow.entity.User;

import java.util.List;

public interface NotificationService {

    /** Used internally by other services to raise a notification for a user. */
    void notify(User user, String message, String type);

    List<NotificationResponse> getNotificationsForUser(Long userId);

    /** Owner-checked: throws UnauthorizedActionException if the notification isn't the requester's. */
    void markAsRead(Long notificationId, Long requesterId);

    void markAllAsRead(Long userId);
}
