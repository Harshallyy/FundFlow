package com.fundflow.service.impl;

import com.fundflow.dto.campaign.*;
import com.fundflow.entity.Campaign;
import com.fundflow.entity.CampaignStatus;
import com.fundflow.entity.User;
import com.fundflow.exception.InvalidCampaignStateException;
import com.fundflow.exception.ResourceNotFoundException;
import com.fundflow.exception.UnauthorizedActionException;
import com.fundflow.repository.CampaignRepository;
import com.fundflow.repository.DonationRepository;
import com.fundflow.repository.UserRepository;
import com.fundflow.service.CampaignService;
import com.fundflow.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CampaignServiceImpl implements CampaignService {

    private static final Set<CampaignStatus> EDITABLE_STATUSES = Set.of(CampaignStatus.DRAFT, CampaignStatus.REJECTED);

    private final CampaignRepository campaignRepository;
    private final DonationRepository donationRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public CampaignResponse createDraft(User organizer, CampaignCreateRequest request) {
        Campaign campaign = Campaign.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .targetAmount(request.getTargetAmount())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .organizer(organizer)
                .beneficiaryName(request.getBeneficiaryName())
                .beneficiaryInfo(request.getBeneficiaryInfo())
                .imagePath(StringUtils.hasText(request.getImagePath())
                        ? request.getImagePath()
                        : "/assets/images/campaign-placeholder-default.svg")
                .status(CampaignStatus.DRAFT)
                .build();

        campaign = campaignRepository.save(campaign);
        return toDetailResponse(campaign);
    }

    @Override
    @Transactional
    public CampaignResponse updateDraft(User organizer, Long campaignId, CampaignEditRequest request) {
        Campaign campaign = getOwnedCampaign(organizer, campaignId);

        if (!EDITABLE_STATUSES.contains(campaign.getStatus())) {
            throw new InvalidCampaignStateException(
                    "Campaign cannot be edited while in status " + campaign.getStatus());
        }

        campaign.setTitle(request.getTitle());
        campaign.setDescription(request.getDescription());
        campaign.setCategory(request.getCategory());
        campaign.setTargetAmount(request.getTargetAmount());
        campaign.setStartDate(request.getStartDate());
        campaign.setEndDate(request.getEndDate());
        campaign.setBeneficiaryName(request.getBeneficiaryName());
        campaign.setBeneficiaryInfo(request.getBeneficiaryInfo());
        if (StringUtils.hasText(request.getImagePath())) {
            campaign.setImagePath(request.getImagePath());
        }

        campaign = campaignRepository.save(campaign);
        return toDetailResponse(campaign);
    }

    @Override
    @Transactional
    public CampaignResponse submitForReview(User organizer, Long campaignId) {
        Campaign campaign = getOwnedCampaign(organizer, campaignId);

        if (!EDITABLE_STATUSES.contains(campaign.getStatus())) {
            throw new InvalidCampaignStateException(
                    "Only DRAFT or REJECTED campaigns can be submitted for review (current status: "
                            + campaign.getStatus() + ")");
        }

        campaign.setStatus(CampaignStatus.PENDING);
        campaign.setReviewNotes(null);
        campaign = campaignRepository.save(campaign);

        notificationService.notify(organizer,
                "Your campaign \"" + campaign.getTitle() + "\" was submitted and is awaiting admin review.",
                "CAMPAIGN_SUBMITTED");

        String campaignTitle = campaign.getTitle();

        userRepository.findByRole_Name("ROLE_ADMIN").forEach(admin -> notificationService.notify(admin,
                "New campaign \"" + campaignTitle + "\" is pending review.",
                "CAMPAIGN_PENDING_REVIEW"));

        return toDetailResponse(campaign);
    }

    @Override
    @Transactional
    public CampaignResponse reviewCampaign(Long campaignId, CampaignReviewRequest request) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found: " + campaignId));

        String action = request.getAction().toUpperCase();

        switch (action) {
            case "APPROVE" -> {
                requireStatus(campaign, CampaignStatus.PENDING, "approved");
                campaign.setStatus(CampaignStatus.APPROVED);
                campaign.setReviewNotes(request.getReviewNotes());
                notificationService.notify(campaign.getOrganizer(),
                        "Your campaign \"" + campaign.getTitle() + "\" has been approved and is now live.",
                        "CAMPAIGN_APPROVED");
            }
            case "REJECT" -> {
                requireStatus(campaign, CampaignStatus.PENDING, "rejected");
                requireReviewNotes(request);
                campaign.setStatus(CampaignStatus.REJECTED);
                campaign.setReviewNotes(request.getReviewNotes());
                notificationService.notify(campaign.getOrganizer(),
                        "Your campaign \"" + campaign.getTitle() + "\" was rejected: " + request.getReviewNotes(),
                        "CAMPAIGN_REJECTED");
            }
            case "BLOCK" -> {
                if (campaign.getStatus() != CampaignStatus.PENDING && campaign.getStatus() != CampaignStatus.APPROVED) {
                    throw new InvalidCampaignStateException(
                            "Only PENDING or APPROVED campaigns can be blocked (current status: "
                                    + campaign.getStatus() + ")");
                }
                requireReviewNotes(request);
                campaign.setStatus(CampaignStatus.BLOCKED);
                campaign.setReviewNotes(request.getReviewNotes());
                notificationService.notify(campaign.getOrganizer(),
                        "Your campaign \"" + campaign.getTitle() + "\" was blocked: " + request.getReviewNotes(),
                        "CAMPAIGN_BLOCKED");
            }
            default -> throw new InvalidCampaignStateException("Unknown review action: " + request.getAction());
        }

        campaign = campaignRepository.save(campaign);
        return toDetailResponse(campaign);
    }

    @Override
    @Transactional
    public CampaignResponse completeCampaign(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found: " + campaignId));

        if (campaign.getStatus() != CampaignStatus.APPROVED) {
            throw new InvalidCampaignStateException(
                    "Only APPROVED campaigns can be marked COMPLETED (current status: " + campaign.getStatus() + ")");
        }

        campaign.setStatus(CampaignStatus.COMPLETED);
        campaign = campaignRepository.save(campaign);

        notificationService.notify(campaign.getOrganizer(),
                "Your campaign \"" + campaign.getTitle() + "\" has been marked as completed.",
                "CAMPAIGN_COMPLETED");

        return toDetailResponse(campaign);
    }

    @Override
    @Transactional(readOnly = true)
    public CampaignResponse getCampaignDetails(Long campaignId, User requester) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found: " + campaignId));

        if (campaign.getStatus() != CampaignStatus.APPROVED) {
            boolean isOwner = requester != null && campaign.getOrganizer().getId().equals(requester.getId());
            boolean isAdmin = requester != null && "ROLE_ADMIN".equals(requester.getRole().getName());
            if (!isOwner && !isAdmin) {
                throw new ResourceNotFoundException("Campaign not found: " + campaignId);
            }
        }

        return toDetailResponse(campaign);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignSummaryResponse> browseApprovedCampaigns(String category, String keyword) {
        return campaignRepository.searchApproved(CampaignStatus.APPROVED, category, keyword).stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignSummaryResponse> listByOrganizer(Long organizerId) {
        return campaignRepository.findByOrganizerId(organizerId).stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignSummaryResponse> listPendingForAdmin() {
        return campaignRepository.findByStatus(CampaignStatus.PENDING).stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignSummaryResponse> listAllForAdmin(String status) {
        List<Campaign> campaigns = StringUtils.hasText(status)
                ? campaignRepository.findByStatus(CampaignStatus.valueOf(status.toUpperCase()))
                : campaignRepository.findAll();
        return campaigns.stream().map(this::toSummaryResponse).toList();
    }

    // ---- helpers ----

    private Campaign getOwnedCampaign(User organizer, Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found: " + campaignId));
        if (!campaign.getOrganizer().getId().equals(organizer.getId())) {
            throw new UnauthorizedActionException("You do not own this campaign");
        }
        return campaign;
    }

    private void requireStatus(Campaign campaign, CampaignStatus required, String actionPastTense) {
        if (campaign.getStatus() != required) {
            throw new InvalidCampaignStateException(
                    "Only " + required + " campaigns can be " + actionPastTense
                            + " (current status: " + campaign.getStatus() + ")");
        }
    }

    private void requireReviewNotes(CampaignReviewRequest request) {
        if (!StringUtils.hasText(request.getReviewNotes())) {
            throw new InvalidCampaignStateException("Review notes are required for this action");
        }
    }

    private int computeProgress(Campaign campaign) {
        if (campaign.getTargetAmount() == null || campaign.getTargetAmount().signum() == 0) {
            return 0;
        }
        BigDecimal percentage = campaign.getCurrentAmount()
                .divide(campaign.getTargetAmount(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        return percentage.min(BigDecimal.valueOf(100)).intValue();
    }

    private int computeDaysRemaining(Campaign campaign) {
        long days = ChronoUnit.DAYS.between(LocalDate.now(), campaign.getEndDate());
        return (int) Math.max(days, 0);
    }

    private CampaignSummaryResponse toSummaryResponse(Campaign campaign) {
        return CampaignSummaryResponse.builder()
                .id(campaign.getId())
                .title(campaign.getTitle())
                .category(campaign.getCategory())
                .targetAmount(campaign.getTargetAmount())
                .currentAmount(campaign.getCurrentAmount())
                .progressPercentage(computeProgress(campaign))
                .imagePath(campaign.getImagePath())
                .status(campaign.getStatus().name())
                .daysRemaining(computeDaysRemaining(campaign))
                .build();
    }

    private CampaignResponse toDetailResponse(Campaign campaign) {
        int donorCount = (int) donationRepository.countDistinctDonorsForCampaign(campaign.getId());
        return CampaignResponse.builder()
                .id(campaign.getId())
                .title(campaign.getTitle())
                .description(campaign.getDescription())
                .category(campaign.getCategory())
                .targetAmount(campaign.getTargetAmount())
                .currentAmount(campaign.getCurrentAmount())
                .progressPercentage(computeProgress(campaign))
                .donorCount(donorCount)
                .daysRemaining(computeDaysRemaining(campaign))
                .startDate(campaign.getStartDate())
                .endDate(campaign.getEndDate())
                .organizerId(campaign.getOrganizer().getId())
                .organizerName(campaign.getOrganizer().getFullName())
                .beneficiaryName(campaign.getBeneficiaryName())
                .beneficiaryInfo(campaign.getBeneficiaryInfo())
                .imagePath(campaign.getImagePath())
                .status(campaign.getStatus().name())
                .reviewNotes(campaign.getReviewNotes())
                .createdAt(campaign.getCreatedAt())
                .updatedAt(campaign.getUpdatedAt())
                .build();
    }
}
