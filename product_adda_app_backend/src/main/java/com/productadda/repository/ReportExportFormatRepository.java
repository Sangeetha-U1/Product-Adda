package com.productadda.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.ReportExportFormat;

public interface ReportExportFormatRepository extends JpaRepository<ReportExportFormat, UUID> {

    Optional<ReportExportFormat> findByFormatName(String formatName);

}
