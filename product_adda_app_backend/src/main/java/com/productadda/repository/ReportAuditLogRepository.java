package com.productadda.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.ReportAuditLog;

public interface ReportAuditLogRepository extends JpaRepository<ReportAuditLog, UUID> {

}
