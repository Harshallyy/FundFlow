package com.fundflow.controller;

import com.fundflow.dto.analytics.OrganizerStatsResponse;
import com.fundflow.security.CustomUserDetails;
import com.fundflow.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Organizer-only (enforced in SecurityConfig via /api/organizer/**)
@RestController
@RequestMapping("/api/organizer")
@RequiredArgsConstructor
public class OrganizerController {

    private final AnalyticsService analyticsService;

    @GetMapping("/stats")
    public ResponseEntity<OrganizerStatsResponse> stats(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(analyticsService.getOrganizerStats(principal.getUser().getId()));
    }
}
