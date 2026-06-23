package com.productadda.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.Vendor;
import com.productadda.entity.User;

public interface VendorRepository
        extends JpaRepository<Vendor, UUID> {

    boolean existsByFkUser(User fkUser);

    boolean existsByGstNumber(String gstNumber);

    Optional<Vendor> findByFkUser(User fkUser);

    boolean existsByBusinessNameAndPkVendorIdNot(String businessName, UUID vendorId);
}