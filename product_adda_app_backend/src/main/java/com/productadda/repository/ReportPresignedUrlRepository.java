package com.productadda.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.ReportPresignedUrl;

public interface ReportPresignedUrlRepository extends JpaRepository<ReportPresignedUrl, UUID> {

    // Find the active cache entry for the targeted report asset context
    Optional<ReportPresignedUrl> findByFkReport_PkReportIdAndIsActiveTrue(UUID reportId);

}
