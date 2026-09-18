package com.fundflow.controller;

import com.fundflow.dto.notification.NotificationResponse;
import com.fundflow.security.CustomUserDetails;
import com.fundflow.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// No role restriction beyond being logged in - every role has its own notifications.
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> list(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(notificationService.getNotificationsForUser(principal.getUser().getId()));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long id,
                                          @AuthenticationPrincipal CustomUserDetails principal) {
        notificationService.markAsRead(id, principal.getUser().getId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllRead(@AuthenticationPrincipal CustomUserDetails principal) {
        notificationService.markAllAsRead(principal.getUser().getId());
        return ResponseEntity.noContent().build();
    }
}
