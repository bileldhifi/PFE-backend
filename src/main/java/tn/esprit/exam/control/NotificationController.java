package tn.esprit.exam.control;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.esprit.exam.dto.NotificationResponse;
import tn.esprit.exam.service.INotificationService;
import tn.esprit.exam.service.IUserService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {
    
    private final INotificationService notificationService;
    private final IUserService userService;
    
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            Authentication auth
    ) {
        UUID userId = userService.getUserIdByEmail(auth.getName());
        log.info("Fetching notifications for user: {}", userId);
        return ResponseEntity.ok(notificationService.getNotifications(userId));
    }
    
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications(
            Authentication auth
    ) {
        UUID userId = userService.getUserIdByEmail(auth.getName());
        log.info("Fetching unread notifications for user: {}", userId);
        return ResponseEntity.ok(notificationService.getUnreadNotifications(userId));
    }
    
    @GetMapping("/unread/count")
    public ResponseEntity<Long> getUnreadCount(
            Authentication auth
    ) {
        UUID userId = userService.getUserIdByEmail(auth.getName());
        log.info("Fetching unread count for user: {}", userId);
        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
    }
    
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable UUID notificationId,
            Authentication auth
    ) {
        UUID userId = userService.getUserIdByEmail(auth.getName());
        log.info("Marking notification {} as read for user: {}", notificationId, userId);
        notificationService.markAsRead(notificationId, userId);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            Authentication auth
    ) {
        UUID userId = userService.getUserIdByEmail(auth.getName());
        log.info("Marking all notifications as read for user: {}", userId);
        notificationService.markAllAsRead(userId);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable UUID notificationId,
            Authentication auth
    ) {
        UUID userId = userService.getUserIdByEmail(auth.getName());
        log.info("Deleting notification {} for user: {}", notificationId, userId);
        notificationService.deleteNotification(notificationId, userId);
        return ResponseEntity.noContent().build();
    }
}

