package com.productadda.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.TaxConfiguration;

public interface TaxConfigurationRepository extends JpaRepository<TaxConfiguration, UUID> {

    Optional<TaxConfiguration> findByRegionNameAndIsActiveTrue(String regionName);
}