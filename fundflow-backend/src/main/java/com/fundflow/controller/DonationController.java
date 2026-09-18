package com.fundflow.controller;

import com.fundflow.dto.analytics.DonorStatsResponse;
import com.fundflow.dto.donation.DonateRequest;
import com.fundflow.dto.donation.DonationResponse;
import com.fundflow.security.CustomUserDetails;
import com.fundflow.service.AnalyticsService;
import com.fundflow.service.DonationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/donations")
@RequiredArgsConstructor
public class DonationController {

    private final DonationService donationService;
    private final AnalyticsService analyticsService;

    // Donor only (enforced in SecurityConfig)
    @PostMapping
    public ResponseEntity<DonationResponse> donate(
            @Valid @RequestBody DonateRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        DonationResponse response = donationService.donate(principal.getUser(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Donor only - their own donation history
    @GetMapping("/my")
    public ResponseEntity<List<DonationResponse>> myDonations(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(donationService.getDonationHistory(principal.getUser().getId()));
    }

    // Donor only - dashboard summary card
    @GetMapping("/stats")
    public ResponseEntity<DonorStatsResponse> myStats(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(analyticsService.getDonorStats(principal.getUser().getId()));
    }

    // Organizer (own campaign only) or admin (any campaign)
    @GetMapping("/campaign/{campaignId}")
    public ResponseEntity<List<DonationResponse>> forCampaign(
            @PathVariable Long campaignId,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(donationService.getDonationsForCampaign(campaignId, principal.getUser()));
    }
}
