package tn.esprit.exam.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.exam.dto.NotificationResponse;
import tn.esprit.exam.dto.NotificationUpdate;
import tn.esprit.exam.entity.Notification;
import tn.esprit.exam.entity.NotificationType;
import tn.esprit.exam.entity.User;
import tn.esprit.exam.repository.NotificationRepository;
import tn.esprit.exam.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements INotificationService {
    
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    
    @Override
    @Transactional
    public NotificationResponse createNotification(
            UUID userId,
            UUID actorId,
            NotificationType type,
            UUID postId,
            UUID commentId
    ) {
        log.info("Creating notification - userId: {}, actorId: {}, type: {}", 
                userId, actorId, type);
        
        // Don't notify user about their own actions
        if (userId.equals(actorId)) {
            log.debug("Skipping self-notification");
            return null;
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new RuntimeException("Actor not found"));
        
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setActor(actor);
        notification.setType(type);
        notification.setPostId(postId);
        notification.setCommentId(commentId);
        notification.setContent(notification.generateContent());
        notification.setIsRead(false);
        
        Notification saved = notificationRepository.save(notification);
        
        // Get unread count
        Long unreadCount = notificationRepository.countUnreadByUserId(userId);
        
        // Send real-time notification via WebSocket
        NotificationUpdate update = new NotificationUpdate(
                saved.getId(),
                userId,
                actorId,
                actor.getUsername(),
                actor.getAvatarMedia() != null ? actor.getAvatarMedia().getUrl() : null,
                type,
                postId,
                commentId,
                saved.getContent(),
                unreadCount
        );
        
        messagingTemplate.convertAndSend(
                "/topic/notifications/" + userId,
                update
        );
        
        log.info("Notification created and sent via WebSocket: {}", saved.getId());
        
        return mapToResponse(saved);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(UUID userId) {
        log.info("Fetching notifications for user: {}", userId);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications(UUID userId) {
        log.info("Fetching unread notifications for user: {}", userId);
        return notificationRepository.findUnreadByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long getUnreadCount(UUID userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }
    
    @Override
    @Transactional
    public void markAsRead(UUID notificationId, UUID userId) {
        log.info("Marking notification {} as read for user {}", notificationId, userId);
        
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("Not authorized to mark this notification as read");
        }
        
        notificationRepository.markAsRead(notificationId);
        
        // Send update via WebSocket
        Long unreadCount = notificationRepository.countUnreadByUserId(userId);
        NotificationUpdate update = new NotificationUpdate(
                notificationId,
                userId,
                notification.getActor().getId(),
                notification.getActor().getUsername(),
                notification.getActor().getAvatarMedia() != null 
                        ? notification.getActor().getAvatarMedia().getUrl() : null,
                notification.getType(),
                notification.getPostId(),
                notification.getCommentId(),
                notification.getContent(),
                unreadCount
        );
        
        messagingTemplate.convertAndSend(
                "/topic/notifications/" + userId,
                update
        );
    }
    
    @Override
    @Transactional
    public void markAllAsRead(UUID userId) {
        log.info("Marking all notifications as read for user: {}", userId);
        notificationRepository.markAllAsRead(userId);
        
        // Send update via WebSocket
        Long unreadCount = 0L;
        NotificationUpdate update = new NotificationUpdate(
                null,
                userId,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                unreadCount
        );
        
        messagingTemplate.convertAndSend(
                "/topic/notifications/" + userId,
                update
        );
    }
    
    @Override
    @Transactional
    public void deleteNotification(UUID notificationId, UUID userId) {
        log.info("Deleting notification {} for user {}", notificationId, userId);
        
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("Not authorized to delete this notification");
        }
        
        notificationRepository.delete(notification);
    }
    
    private NotificationResponse mapToResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getActor().getId(),
                notification.getActor().getUsername(),
                notification.getActor().getAvatarMedia() != null 
                        ? notification.getActor().getAvatarMedia().getUrl() : null,
                notification.getType(),
                notification.getCreatedAt(),
                notification.getIsRead(),
                notification.getPostId(),
                notification.getCommentId(),
                notification.getContent()
        );
    }
}

