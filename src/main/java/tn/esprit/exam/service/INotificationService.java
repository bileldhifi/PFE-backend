package tn.esprit.exam.service;

import tn.esprit.exam.dto.NotificationResponse;
import tn.esprit.exam.entity.NotificationType;

import java.util.List;
import java.util.UUID;

public interface INotificationService {
    
    /**
     * Create a notification
     */
    NotificationResponse createNotification(
            UUID userId,
            UUID actorId,
            NotificationType type,
            UUID postId,
            UUID commentId
    );
    
    /**
     * Get all notifications for a user
     */
    List<NotificationResponse> getNotifications(UUID userId);
    
    /**
     * Get unread notifications for a user
     */
    List<NotificationResponse> getUnreadNotifications(UUID userId);
    
    /**
     * Get unread notification count
     */
    Long getUnreadCount(UUID userId);
    
    /**
     * Mark notification as read
     */
    void markAsRead(UUID notificationId, UUID userId);
    
    /**
     * Mark all notifications as read for a user
     */
    void markAllAsRead(UUID userId);
    
    /**
     * Delete a notification
     */
    void deleteNotification(UUID notificationId, UUID userId);
}

