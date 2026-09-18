package com.fundflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "NOTIFICATION")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NOTIFICATION_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @Column(name = "MESSAGE", nullable = false, length = 500)
    private String message;

    /** e.g. DONATION_SUCCESS, CAMPAIGN_APPROVED, NEW_DONATION, CAMPAIGN_PENDING_REVIEW */
    @Column(name = "TYPE", nullable = false, length = 50)
    private String type;

    @Column(name = "IS_READ", nullable = false)
    @Builder.Default
    private boolean read = false;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
