package com.fundflow.repository;

import com.fundflow.entity.Campaign;
import com.fundflow.entity.CampaignStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    List<Campaign> findByStatus(CampaignStatus status);

    long countByStatus(CampaignStatus status);

    List<Campaign> findByOrganizerId(Long organizerId);

    List<Campaign> findByOrganizerIdAndStatus(Long organizerId, CampaignStatus status);

    // Simple public search: only ever called with status = APPROVED from the service layer.
    @Query("""
           SELECT c FROM Campaign c
           WHERE c.status = :status
           AND (:category IS NULL OR c.category = :category)
           AND (:keyword IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
           """)
    List<Campaign> searchApproved(@Param("status") CampaignStatus status,
                                   @Param("category") String category,
                                   @Param("keyword") String keyword);
}
