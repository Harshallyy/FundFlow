package com.fundflow.controller;

import com.fundflow.dto.analytics.AdminStatsResponse;
import com.fundflow.dto.donation.DonationResponse;
import com.fundflow.dto.user.UserResponse;
import com.fundflow.service.AnalyticsService;
import com.fundflow.service.DonationService;
import com.fundflow.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// All endpoints here are admin-only (enforced in SecurityConfig via /api/admin/**)
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final AnalyticsService analyticsService;
    private final DonationService donationService;

    // e.g. GET /api/admin/users, or GET /api/admin/users?role=ORGANIZER
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> listUsers(@RequestParam(required = false) String role) {
        return ResponseEntity.ok(userService.listUsers(role));
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> stats() {
        return ResponseEntity.ok(analyticsService.getAdminStats());
    }

    // Doubles as both "Donations" and "Transactions" views from the spec - each
    // DonationResponse already carries its mockReference and transactionRef.
    @GetMapping("/donations")
    public ResponseEntity<List<DonationResponse>> allDonations() {
        return ResponseEntity.ok(donationService.getAllDonations());
    }
}
