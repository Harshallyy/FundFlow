package com.fundflow.repository;

import com.fundflow.entity.CampaignUpdate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CampaignUpdateRepository extends JpaRepository<CampaignUpdate, Long> {
    List<CampaignUpdate> findByCampaignIdOrderByCreatedAtDesc(Long campaignId);
}
