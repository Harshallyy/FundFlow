package com.fundflow.repository;

import com.fundflow.entity.SavedCampaign;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavedCampaignRepository extends JpaRepository<SavedCampaign, Long> {
    Optional<SavedCampaign> findByDonorIdAndCampaignId(Long donorId, Long campaignId);
    boolean existsByDonorIdAndCampaignId(Long donorId, Long campaignId);
    List<SavedCampaign> findByDonorId(Long donorId);
    void deleteByDonorIdAndCampaignId(Long donorId, Long campaignId);
}
