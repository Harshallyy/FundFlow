package com.fundflow.service;

import com.fundflow.dto.donation.DonateRequest;
import com.fundflow.dto.donation.DonationResponse;
import com.fundflow.entity.User;

import java.util.List;

public interface DonationService {

    DonationResponse donate(User donor, DonateRequest request);

    List<DonationResponse> getDonationHistory(Long donorId);

    /**
     * Donations received on a single campaign - used by the owning organizer's
     * "Donations Received" view and by admins. Organizers may only view their
     * own campaign's donations; admins may view any.
     */
    List<DonationResponse> getDonationsForCampaign(Long campaignId, User requester);

    /** Admin-only: every donation on the platform, most recent first. */
    List<DonationResponse> getAllDonations();
}
