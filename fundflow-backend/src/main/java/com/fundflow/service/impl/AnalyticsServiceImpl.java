package com.fundflow.service.impl;

import com.fundflow.dto.analytics.AdminStatsResponse;
import com.fundflow.dto.analytics.DonorStatsResponse;
import com.fundflow.dto.analytics.OrganizerStatsResponse;
import com.fundflow.dto.donation.DonationResponse;
import com.fundflow.entity.CampaignStatus;
import com.fundflow.entity.Donation;
import com.fundflow.entity.Payment;
import com.fundflow.entity.TransactionLog;
import com.fundflow.entity.TxnStatus;
import com.fundflow.repository.CampaignRepository;
import com.fundflow.repository.DonationRepository;
import com.fundflow.repository.PaymentRepository;
import com.fundflow.repository.TransactionLogRepository;
import com.fundflow.repository.UserRepository;
import com.fundflow.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    private final CampaignRepository campaignRepository;
    private final DonationRepository donationRepository;
    private final PaymentRepository paymentRepository;
    private final TransactionLogRepository transactionLogRepository;
    private final UserRepository userRepository;

    @Override
    public OrganizerStatsResponse getOrganizerStats(Long organizerId) {
        return OrganizerStatsResponse.builder()
                .totalRaised(donationRepository.sumSuccessfulAmountForOrganizer(organizerId))
                .totalDonors(donationRepository.countDistinctDonorsForOrganizer(organizerId))
                .donationCount(donationRepository.countSuccessfulForOrganizer(organizerId))
                .campaignCount(campaignRepository.findByOrganizerId(organizerId).size())
                .approvedCampaignCount(campaignRepository.findByOrganizerIdAndStatus(organizerId, CampaignStatus.APPROVED).size())
                .pendingCampaignCount(campaignRepository.findByOrganizerIdAndStatus(organizerId, CampaignStatus.PENDING).size())
                .build();
    }

    @Override
    public AdminStatsResponse getAdminStats() {
        return AdminStatsResponse.builder()
                .totalUsers(userRepository.count())
                .totalCampaigns(campaignRepository.count())
                .approvedCampaigns(campaignRepository.countByStatus(CampaignStatus.APPROVED))
                .pendingCampaigns(campaignRepository.countByStatus(CampaignStatus.PENDING))
                .rejectedCampaigns(campaignRepository.countByStatus(CampaignStatus.REJECTED))
                .blockedCampaigns(campaignRepository.countByStatus(CampaignStatus.BLOCKED))
                .completedCampaigns(campaignRepository.countByStatus(CampaignStatus.COMPLETED))
                .totalSuccessfulDonations(donationRepository.countByStatus(TxnStatus.SUCCESS))
                .totalTransactionAmount(donationRepository.sumAllSuccessfulAmount())
                .build();
    }

    @Override
    public DonorStatsResponse getDonorStats(Long donorId) {
        List<DonationResponse> recent = donationRepository.findByDonorIdOrderByCreatedAtDesc(donorId).stream()
                .limit(5)
                .map(this::toResponse)
                .toList();

        return DonorStatsResponse.builder()
                .totalDonated(donationRepository.sumSuccessfulAmountForDonor(donorId))
                .donationCount(donationRepository.countByDonorIdAndStatus(donorId, TxnStatus.SUCCESS))
                .recentDonations(recent)
                .build();
    }

    // Same mapping shape as DonationServiceImpl - kept small and local rather than
    // introducing a shared mapper class for just this read-only dashboard view.
    private DonationResponse toResponse(Donation donation) {
        Payment payment = paymentRepository.findByDonationId(donation.getId()).orElse(null);
        String mockRef = payment != null ? payment.getMockReference() : null;
        String txnRef = null;
        if (payment != null) {
            txnRef = transactionLogRepository.findByPaymentId(payment.getId()).stream()
                    .findFirst()
                    .map(TransactionLog::getTransactionRef)
                    .orElse(null);
        }
        return DonationResponse.builder()
                .donationId(donation.getId())
                .campaignId(donation.getCampaign().getId())
                .campaignTitle(donation.getCampaign().getTitle())
                .amount(donation.getAmount())
                .status(donation.getStatus().name())
                .mockReference(mockRef)
                .transactionRef(txnRef)
                .createdAt(donation.getCreatedAt())
                .build();
    }
}
