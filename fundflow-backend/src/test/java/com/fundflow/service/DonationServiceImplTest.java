package com.fundflow.service;

import com.fundflow.dto.donation.DonateRequest;
import com.fundflow.entity.*;
import com.fundflow.exception.InvalidCampaignStateException;
import com.fundflow.payment.PaymentResult;
import com.fundflow.payment.PaymentService;
import com.fundflow.repository.CampaignRepository;
import com.fundflow.repository.DonationRepository;
import com.fundflow.repository.PaymentRepository;
import com.fundflow.repository.TransactionLogRepository;
import com.fundflow.service.impl.DonationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DonationServiceImplTest {

    @Mock private CampaignRepository campaignRepository;
    @Mock private DonationRepository donationRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private TransactionLogRepository transactionLogRepository;
    @Mock private PaymentService paymentService;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private DonationServiceImpl donationService;

    private User donor;
    private User organizer;
    private Campaign campaign;

    @BeforeEach
    void setUp() {
        Role donorRole = Role.builder().id(1L).name("ROLE_DONOR").build();
        Role organizerRole = Role.builder().id(2L).name("ROLE_ORGANIZER").build();
        donor = User.builder().id(5L).fullName("Donor One").email("donor@test.com").role(donorRole).build();
        organizer = User.builder().id(1L).fullName("Org One").email("org@test.com").role(organizerRole).build();

        campaign = Campaign.builder()
                .id(10L)
                .title("Help build a well")
                .targetAmount(BigDecimal.valueOf(1000))
                .currentAmount(BigDecimal.ZERO)
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(10))
                .organizer(organizer)
                .beneficiaryName("Village Trust")
                .status(CampaignStatus.APPROVED)
                .build();
    }

    @Test
    void donate_toApprovedCampaign_updatesCampaignAmountOnSuccess() {
        DonateRequest request = new DonateRequest();
        request.setCampaignId(10L);
        request.setAmount(BigDecimal.valueOf(100));
        request.setPaymentMethod("MOCK_CARD");

        when(campaignRepository.findById(10L)).thenReturn(Optional.of(campaign));
        when(donationRepository.save(any(Donation.class))).thenAnswer(inv -> {
            Donation d = inv.getArgument(0);
            d.setId(99L);
            return d;
        });
        when(paymentService.processPayment(any(), anyString()))
                .thenReturn(new PaymentResult(TxnStatus.SUCCESS, "MOCK-REF-123"));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionLogRepository.save(any(TransactionLog.class))).thenAnswer(inv -> inv.getArgument(0));
        when(campaignRepository.save(any(Campaign.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = donationService.donate(donor, request);

        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(campaign.getCurrentAmount()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    void donate_toNonApprovedCampaign_throws() {
        campaign.setStatus(CampaignStatus.PENDING);
        DonateRequest request = new DonateRequest();
        request.setCampaignId(10L);
        request.setAmount(BigDecimal.valueOf(50));
        request.setPaymentMethod("MOCK_UPI");

        when(campaignRepository.findById(10L)).thenReturn(Optional.of(campaign));

        assertThatThrownBy(() -> donationService.donate(donor, request))
                .isInstanceOf(InvalidCampaignStateException.class);
    }

    @Test
    void donate_toExpiredCampaign_throws() {
        campaign.setEndDate(LocalDate.now().minusDays(1));
        DonateRequest request = new DonateRequest();
        request.setCampaignId(10L);
        request.setAmount(BigDecimal.valueOf(50));
        request.setPaymentMethod("MOCK_UPI");

        when(campaignRepository.findById(10L)).thenReturn(Optional.of(campaign));

        assertThatThrownBy(() -> donationService.donate(donor, request))
                .isInstanceOf(InvalidCampaignStateException.class);
    }
}
