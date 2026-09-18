package com.fundflow.service.impl;

import com.fundflow.dto.donation.DonateRequest;
import com.fundflow.dto.donation.DonationResponse;
import com.fundflow.entity.*;
import com.fundflow.exception.InvalidCampaignStateException;
import com.fundflow.exception.ResourceNotFoundException;
import com.fundflow.exception.UnauthorizedActionException;
import com.fundflow.payment.PaymentResult;
import com.fundflow.payment.PaymentService;
import com.fundflow.repository.CampaignRepository;
import com.fundflow.repository.DonationRepository;
import com.fundflow.repository.PaymentRepository;
import com.fundflow.repository.TransactionLogRepository;
import com.fundflow.service.DonationService;
import com.fundflow.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DonationServiceImpl implements DonationService {

    private final CampaignRepository campaignRepository;
    private final DonationRepository donationRepository;
    private final PaymentRepository paymentRepository;
    private final TransactionLogRepository transactionLogRepository;
    private final PaymentService paymentService; // MockPaymentServiceImpl for now
    private final NotificationService notificationService;

    @Override
    @Transactional
    public DonationResponse donate(User donor, DonateRequest request) {
        Campaign campaign = campaignRepository.findById(request.getCampaignId())
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found: " + request.getCampaignId()));

        if (campaign.getStatus() != CampaignStatus.APPROVED) {
            throw new InvalidCampaignStateException(
                    "Cannot donate to a campaign in status " + campaign.getStatus());
        }
        if (campaign.getEndDate().isBefore(LocalDate.now())) {
            throw new InvalidCampaignStateException("Cannot donate to a campaign that has already ended");
        }

        // 1. Record the donation attempt as PENDING first.
        Donation donation = Donation.builder()
                .campaign(campaign)
                .donor(donor)
                .amount(request.getAmount())
                .status(TxnStatus.PENDING)
                .build();
        donation = donationRepository.save(donation);

        // 2. Run it through the (mock) payment gateway.
        PaymentResult result = paymentService.processPayment(donation, request.getPaymentMethod());

        // 3. Persist the Payment record.
        Payment payment = Payment.builder()
                .donation(donation)
                .method(request.getPaymentMethod())
                .status(result.status())
                .mockReference(result.reference())
                .paidAt(result.status() == TxnStatus.SUCCESS ? LocalDateTime.now() : null)
                .build();
        payment = paymentRepository.save(payment);

        // 4. Persist the immutable transaction ledger entry.
        TransactionLog transactionLog = TransactionLog.builder()
                .payment(payment)
                .transactionRef("TXN-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase())
                .amount(request.getAmount())
                .status(result.status())
                .build();
        transactionLog = transactionLogRepository.save(transactionLog);

        // 5. Reflect the outcome on the donation, campaign totals, and notifications.
        donation.setStatus(result.status());
        donationRepository.save(donation);

        switch (result.status()) {
            case SUCCESS -> {
                campaign.setCurrentAmount(campaign.getCurrentAmount().add(request.getAmount()));
                campaignRepository.save(campaign);
                notificationService.notify(donor,
                        "Your donation of " + request.getAmount() + " to \"" + campaign.getTitle() + "\" was successful.",
                        "DONATION_SUCCESS");
                notificationService.notify(campaign.getOrganizer(),
                        "New donation of " + request.getAmount() + " received on \"" + campaign.getTitle() + "\".",
                        "NEW_DONATION");
            }
            case FAILED -> notificationService.notify(donor,
                    "Your donation of " + request.getAmount() + " to \"" + campaign.getTitle() + "\" failed. Please try again.",
                    "DONATION_FAILED");
            case PENDING -> notificationService.notify(donor,
                    "Your donation of " + request.getAmount() + " to \"" + campaign.getTitle() + "\" is pending confirmation.",
                    "DONATION_PENDING");
        }

        return toResponse(donation, payment, transactionLog);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DonationResponse> getDonationHistory(Long donorId) {
        return donationRepository.findByDonorIdOrderByCreatedAtDesc(donorId).stream()
                .map(this::toResponseFromDonation)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DonationResponse> getDonationsForCampaign(Long campaignId, User requester) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found: " + campaignId));

        boolean isAdmin = "ROLE_ADMIN".equals(requester.getRole().getName());
        boolean isOwner = campaign.getOrganizer().getId().equals(requester.getId());
        if (!isAdmin && !isOwner) {
            throw new UnauthorizedActionException("You do not own this campaign");
        }

        return donationRepository.findByCampaignIdOrderByCreatedAtDesc(campaignId).stream()
                .map(this::toResponseFromDonation)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DonationResponse> getAllDonations() {
        return donationRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponseFromDonation)
                .toList();
    }

    // ---- helpers ----

    private DonationResponse toResponseFromDonation(Donation donation) {
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

    private DonationResponse toResponse(Donation donation, Payment payment, TransactionLog transactionLog) {
        return DonationResponse.builder()
                .donationId(donation.getId())
                .campaignId(donation.getCampaign().getId())
                .campaignTitle(donation.getCampaign().getTitle())
                .amount(donation.getAmount())
                .status(donation.getStatus().name())
                .mockReference(payment.getMockReference())
                .transactionRef(transactionLog.getTransactionRef())
                .createdAt(donation.getCreatedAt())
                .build();
    }
}
