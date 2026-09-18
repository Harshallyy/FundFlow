package com.fundflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "CAMPAIGN")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CAMPAIGN_ID")
    private Long id;

    @Column(name = "TITLE", nullable = false, length = 200)
    private String title;

    @Lob
    @Column(name = "DESCRIPTION", nullable = false)
    private String description;

    @Column(name = "CATEGORY", length = 50)
    private String category;

    @Column(name = "TARGET_AMOUNT", nullable = false, precision = 12, scale = 2)
    private BigDecimal targetAmount;

    @Column(name = "CURRENT_AMOUNT", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal currentAmount = BigDecimal.ZERO;

    @Column(name = "START_DATE", nullable = false)
    private LocalDate startDate;

    @Column(name = "END_DATE", nullable = false)
    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORGANIZER_ID", nullable = false)
    private User organizer;

    @Column(name = "BENEFICIARY_NAME", nullable = false, length = 150)
    private String beneficiaryName;

    @Column(name = "BENEFICIARY_INFO", length = 1000)
    private String beneficiaryInfo;

    /**
     * Local asset path only, e.g. "/assets/images/campaign-placeholder-1.png".
     * Never an external URL - keeps the app runnable fully offline.
     */
    @Column(name = "IMAGE_PATH", length = 300)
    private String imagePath;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    @Builder.Default
    private CampaignStatus status = CampaignStatus.DRAFT;

    @Column(name = "REVIEW_NOTES", length = 1000)
    private String reviewNotes;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
