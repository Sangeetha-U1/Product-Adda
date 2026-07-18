package com.productadda.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.ReportJobStatus;

public interface ReportJobStatusRepository extends JpaRepository<ReportJobStatus, UUID> {

    Optional<ReportJobStatus> findByStatusName(String statusName);

}
