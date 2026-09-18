package com.fundflow.repository;

import com.fundflow.entity.CampaignDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CampaignDocumentRepository extends JpaRepository<CampaignDocument, Long> {
    List<CampaignDocument> findByCampaignId(Long campaignId);
}
