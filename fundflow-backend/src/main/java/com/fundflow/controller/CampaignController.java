package com.fundflow.controller;

import com.fundflow.dto.campaign.*;
import com.fundflow.security.CustomUserDetails;
import com.fundflow.service.CampaignService;
import com.fundflow.service.CampaignUpdateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/campaigns")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignService campaignService;
    private final CampaignUpdateService campaignUpdateService;

    // ---- Public ----

    @GetMapping("/explore")
    public ResponseEntity<List<CampaignSummaryResponse>> explore(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(campaignService.browseApprovedCampaigns(category, keyword));
    }

    // Public, but the service hides non-APPROVED campaigns from anyone who
    // isn't the owning organizer or an admin - "principal" is null for
    // anonymous visitors, which the service treats as "not the owner".
    @GetMapping("/{id}")
    public ResponseEntity<CampaignResponse> getDetails(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        var requester = principal != null ? principal.getUser() : null;
        return ResponseEntity.ok(campaignService.getCampaignDetails(id, requester));
    }

    @GetMapping("/{id}/updates")
    public ResponseEntity<List<CampaignUpdateResponse>> getUpdates(@PathVariable Long id) {
        return ResponseEntity.ok(campaignUpdateService.getUpdatesForCampaign(id));
    }

    // ---- Organizer ----

    @GetMapping("/mine")
    public ResponseEntity<List<CampaignSummaryResponse>> myCampaigns(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(campaignService.listByOrganizer(principal.getUser().getId()));
    }

    @PostMapping
    public ResponseEntity<CampaignResponse> createDraft(
            @Valid @RequestBody CampaignCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        CampaignResponse response = campaignService.createDraft(principal.getUser(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CampaignResponse> updateDraft(
            @PathVariable Long id,
            @Valid @RequestBody CampaignEditRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(campaignService.updateDraft(principal.getUser(), id, request));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<CampaignResponse> submit(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(campaignService.submitForReview(principal.getUser(), id));
    }

    @PostMapping("/{id}/updates")
    public ResponseEntity<CampaignUpdateResponse> postUpdate(
            @PathVariable Long id,
            @Valid @RequestBody PostCampaignUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        CampaignUpdateResponse response = campaignUpdateService.postUpdate(principal.getUser(), id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ---- Admin ----

    @PostMapping("/{id}/review")
    public ResponseEntity<CampaignResponse> review(
            @PathVariable Long id,
            @Valid @RequestBody CampaignReviewRequest request) {
        return ResponseEntity.ok(campaignService.reviewCampaign(id, request));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<CampaignResponse> complete(@PathVariable Long id) {
        return ResponseEntity.ok(campaignService.completeCampaign(id));
    }

    @GetMapping("/admin/pending")
    public ResponseEntity<List<CampaignSummaryResponse>> pendingForAdmin() {
        return ResponseEntity.ok(campaignService.listPendingForAdmin());
    }

    @GetMapping("/admin")
    public ResponseEntity<List<CampaignSummaryResponse>> allForAdmin(
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(campaignService.listAllForAdmin(status));
    }
}
