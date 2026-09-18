package com.fundflow.service.impl;

import com.fundflow.dto.campaign.CampaignUpdateResponse;
import com.fundflow.dto.campaign.PostCampaignUpdateRequest;
import com.fundflow.entity.Campaign;
import com.fundflow.entity.CampaignStatus;
import com.fundflow.entity.User;
import com.fundflow.exception.InvalidCampaignStateException;
import com.fundflow.exception.ResourceNotFoundException;
import com.fundflow.exception.UnauthorizedActionException;
import com.fundflow.repository.CampaignRepository;
import com.fundflow.repository.CampaignUpdateRepository;
import com.fundflow.service.CampaignUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CampaignUpdateServiceImpl implements CampaignUpdateService {

    private final CampaignUpdateRepository campaignUpdateRepository;
    private final CampaignRepository campaignRepository;

    @Override
    @Transactional
    public CampaignUpdateResponse postUpdate(User organizer, Long campaignId, PostCampaignUpdateRequest request) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found: " + campaignId));

        if (!campaign.getOrganizer().getId().equals(organizer.getId())) {
            throw new UnauthorizedActionException("You do not own this campaign");
        }
        if (campaign.getStatus() != CampaignStatus.APPROVED) {
            throw new InvalidCampaignStateException("Updates can only be posted to APPROVED campaigns");
        }

        com.fundflow.entity.CampaignUpdate update = com.fundflow.entity.CampaignUpdate.builder()
                .campaign(campaign)
                .organizer(organizer)
                .updateText(request.getUpdateText())
                .build();
        update = campaignUpdateRepository.save(update);

        return toResponse(update);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignUpdateResponse> getUpdatesForCampaign(Long campaignId) {
        return campaignUpdateRepository.findByCampaignIdOrderByCreatedAtDesc(campaignId).stream()
                .map(this::toResponse)
                .toList();
    }

    private CampaignUpdateResponse toResponse(com.fundflow.entity.CampaignUpdate update) {
        return CampaignUpdateResponse.builder()
                .id(update.getId())
                .campaignId(update.getCampaign().getId())
                .updateText(update.getUpdateText())
                .createdAt(update.getCreatedAt())
                .build();
    }
}
