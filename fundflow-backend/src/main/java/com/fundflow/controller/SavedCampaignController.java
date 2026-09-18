package com.fundflow.controller;

import com.fundflow.dto.campaign.CampaignSummaryResponse;
import com.fundflow.security.CustomUserDetails;
import com.fundflow.service.SavedCampaignService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// All endpoints here are donor-only (enforced in SecurityConfig via /api/saved-campaigns/**)
@RestController
@RequestMapping("/api/saved-campaigns")
@RequiredArgsConstructor
public class SavedCampaignController {

    private final SavedCampaignService savedCampaignService;

    @GetMapping
    public ResponseEntity<List<CampaignSummaryResponse>> list(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(savedCampaignService.getSavedCampaigns(principal.getUser().getId()));
    }

    @PostMapping("/{campaignId}")
    public ResponseEntity<Void> save(@PathVariable Long campaignId,
                                      @AuthenticationPrincipal CustomUserDetails principal) {
        savedCampaignService.saveCampaign(principal.getUser(), campaignId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{campaignId}")
    public ResponseEntity<Void> unsave(@PathVariable Long campaignId,
                                        @AuthenticationPrincipal CustomUserDetails principal) {
        savedCampaignService.unsaveCampaign(principal.getUser(), campaignId);
        return ResponseEntity.noContent().build();
    }
}
