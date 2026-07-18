package com.productadda.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.Report;

public interface ReportRepository extends JpaRepository<Report, UUID> {

}
