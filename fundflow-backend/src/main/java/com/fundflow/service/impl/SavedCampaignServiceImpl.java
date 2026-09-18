package com.fundflow.service.impl;

import com.fundflow.dto.campaign.CampaignSummaryResponse;
import com.fundflow.entity.Campaign;
import com.fundflow.entity.SavedCampaign;
import com.fundflow.entity.User;
import com.fundflow.exception.DuplicateResourceException;
import com.fundflow.exception.ResourceNotFoundException;
import com.fundflow.repository.CampaignRepository;
import com.fundflow.repository.SavedCampaignRepository;
import com.fundflow.service.SavedCampaignService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SavedCampaignServiceImpl implements SavedCampaignService {

    private final SavedCampaignRepository savedCampaignRepository;
    private final CampaignRepository campaignRepository;

    @Override
    @Transactional
    public void saveCampaign(User donor, Long campaignId) {
        if (savedCampaignRepository.existsByDonorIdAndCampaignId(donor.getId(), campaignId)) {
            throw new DuplicateResourceException("Campaign already saved");
        }
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found: " + campaignId));

        savedCampaignRepository.save(SavedCampaign.builder()
                .donor(donor)
                .campaign(campaign)
                .build());
    }

    @Override
    @Transactional
    public void unsaveCampaign(User donor, Long campaignId) {
        savedCampaignRepository.deleteByDonorIdAndCampaignId(donor.getId(), campaignId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignSummaryResponse> getSavedCampaigns(Long donorId) {
        return savedCampaignRepository.findByDonorId(donorId).stream()
                .map(SavedCampaign::getCampaign)
                .map(this::toSummary)
                .toList();
    }

    // Same computation as CampaignServiceImpl.toSummaryResponse - kept small and
    // duplicated here rather than introducing a shared mapper class for just this.
    private CampaignSummaryResponse toSummary(Campaign campaign) {
        BigDecimal progress = campaign.getTargetAmount().signum() == 0
                ? BigDecimal.ZERO
                : campaign.getCurrentAmount()
                    .divide(campaign.getTargetAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .min(BigDecimal.valueOf(100));
        long daysRemaining = Math.max(ChronoUnit.DAYS.between(LocalDate.now(), campaign.getEndDate()), 0);

        return CampaignSummaryResponse.builder()
                .id(campaign.getId())
                .title(campaign.getTitle())
                .category(campaign.getCategory())
                .targetAmount(campaign.getTargetAmount())
                .currentAmount(campaign.getCurrentAmount())
                .progressPercentage(progress.intValue())
                .imagePath(campaign.getImagePath())
                .status(campaign.getStatus().name())
                .daysRemaining((int) daysRemaining)
                .build();
    }
}
