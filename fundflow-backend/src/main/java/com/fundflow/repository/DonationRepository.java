package com.fundflow.repository;

import com.fundflow.entity.Donation;
import com.fundflow.entity.TxnStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface DonationRepository extends JpaRepository<Donation, Long> {

    List<Donation> findByDonorIdOrderByCreatedAtDesc(Long donorId);

    List<Donation> findByCampaignIdOrderByCreatedAtDesc(Long campaignId);

    List<Donation> findAllByOrderByCreatedAtDesc();

    long countByCampaignIdAndStatus(Long campaignId, TxnStatus status);

    long countByDonorIdAndStatus(Long donorId, TxnStatus status);

    long countByStatus(TxnStatus status);

    @Query("SELECT COUNT(DISTINCT d.donor.id) FROM Donation d WHERE d.campaign.id = :campaignId AND d.status = 'SUCCESS'")
    long countDistinctDonorsForCampaign(Long campaignId);

    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM Donation d WHERE d.donor.id = :donorId AND d.status = 'SUCCESS'")
    BigDecimal sumSuccessfulAmountForDonor(@Param("donorId") Long donorId);

    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM Donation d WHERE d.status = 'SUCCESS'")
    BigDecimal sumAllSuccessfulAmount();

    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM Donation d WHERE d.campaign.organizer.id = :organizerId AND d.status = 'SUCCESS'")
    BigDecimal sumSuccessfulAmountForOrganizer(@Param("organizerId") Long organizerId);

    @Query("SELECT COUNT(d) FROM Donation d WHERE d.campaign.organizer.id = :organizerId AND d.status = 'SUCCESS'")
    long countSuccessfulForOrganizer(@Param("organizerId") Long organizerId);

    @Query("SELECT COUNT(DISTINCT d.donor.id) FROM Donation d WHERE d.campaign.organizer.id = :organizerId AND d.status = 'SUCCESS'")
    long countDistinctDonorsForOrganizer(@Param("organizerId") Long organizerId);
}
