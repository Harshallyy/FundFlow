package com.fundflow.service;

import com.fundflow.dto.campaign.CampaignReviewRequest;
import com.fundflow.entity.*;
import com.fundflow.exception.InvalidCampaignStateException;
import com.fundflow.repository.CampaignRepository;
import com.fundflow.repository.DonationRepository;
import com.fundflow.repository.UserRepository;
import com.fundflow.service.impl.CampaignServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampaignServiceImplTest {

    @Mock private CampaignRepository campaignRepository;
    @Mock private DonationRepository donationRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private CampaignServiceImpl campaignService;

    private User organizer;
    private Campaign campaign;

    @BeforeEach
    void setUp() {
        Role organizerRole = Role.builder().id(2L).name("ROLE_ORGANIZER").build();
        organizer = User.builder().id(1L).fullName("Org One").email("org@test.com").role(organizerRole).build();

        campaign = Campaign.builder()
                .id(10L)
                .title("Help build a well")
                .description("desc")
                .targetAmount(BigDecimal.valueOf(1000))
                .currentAmount(BigDecimal.ZERO)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .organizer(organizer)
                .beneficiaryName("Village Trust")
                .status(CampaignStatus.DRAFT)
                .build();
    }

    @Test
    void submitForReview_movesDraftToPending() {
        when(campaignRepository.findById(10L)).thenReturn(Optional.of(campaign));
        when(campaignRepository.save(any(Campaign.class))).thenAnswer(inv -> inv.getArgument(0));
        when(donationRepository.countDistinctDonorsForCampaign(10L)).thenReturn(0L);
        when(userRepository.findByRole_Name("ROLE_ADMIN")).thenReturn(List.of());

        var response = campaignService.submitForReview(organizer, 10L);

        assertThat(response.getStatus()).isEqualTo("PENDING");
    }

    @Test
    void reviewCampaign_approveFromPending_succeeds() {
        campaign.setStatus(CampaignStatus.PENDING);
        when(campaignRepository.findById(10L)).thenReturn(Optional.of(campaign));
        when(campaignRepository.save(any(Campaign.class))).thenAnswer(inv -> inv.getArgument(0));
        when(donationRepository.countDistinctDonorsForCampaign(10L)).thenReturn(0L);

        CampaignReviewRequest request = new CampaignReviewRequest();
        request.setAction("APPROVE");

        var response = campaignService.reviewCampaign(10L, request);

        assertThat(response.getStatus()).isEqualTo("APPROVED");
        verify(notificationService).notify(eq(organizer), anyString(), eq("CAMPAIGN_APPROVED"));
    }

    @Test
    void reviewCampaign_approveFromDraft_throws() {
        // still DRAFT, not PENDING
        when(campaignRepository.findById(10L)).thenReturn(Optional.of(campaign));

        CampaignReviewRequest request = new CampaignReviewRequest();
        request.setAction("APPROVE");

        assertThatThrownBy(() -> campaignService.reviewCampaign(10L, request))
                .isInstanceOf(InvalidCampaignStateException.class);
    }

    @Test
    void reviewCampaign_rejectWithoutNotes_throws() {
        campaign.setStatus(CampaignStatus.PENDING);
        when(campaignRepository.findById(10L)).thenReturn(Optional.of(campaign));

        CampaignReviewRequest request = new CampaignReviewRequest();
        request.setAction("REJECT"); // no reviewNotes set

        assertThatThrownBy(() -> campaignService.reviewCampaign(10L, request))
                .isInstanceOf(InvalidCampaignStateException.class);
    }

    @Test
    void updateDraft_byNonOwner_throwsUnauthorized() {
        Role otherOrgRole = Role.builder().id(2L).name("ROLE_ORGANIZER").build();
        User someoneElse = User.builder().id(999L).fullName("Not The Owner").email("other@test.com").role(otherOrgRole).build();

        when(campaignRepository.findById(10L)).thenReturn(Optional.of(campaign));

        var request = new com.fundflow.dto.campaign.CampaignEditRequest();
        request.setTitle("Hijacked title");
        request.setDescription("desc");
        request.setTargetAmount(BigDecimal.valueOf(500));
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(5));
        request.setBeneficiaryName("Someone");

        assertThatThrownBy(() -> campaignService.updateDraft(someoneElse, 10L, request))
                .isInstanceOf(com.fundflow.exception.UnauthorizedActionException.class);
    }
}
