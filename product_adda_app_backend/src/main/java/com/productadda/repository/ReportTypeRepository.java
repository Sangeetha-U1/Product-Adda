package com.productadda.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.ReportType;

public interface ReportTypeRepository extends JpaRepository<ReportType, UUID> {

    Optional<ReportType> findByReportTypeName(String reportTypeName);

}
