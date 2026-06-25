package com.productadda.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.productadda.dto.admin.VendorAdminSummaryResponseDto;
import com.productadda.entity.Vendor;
import com.productadda.entity.User;

public interface VendorRepository extends JpaRepository<Vendor, UUID> {

    boolean existsByFkUser(User fkUser);

    boolean existsByGstNumber(String gstNumber);

    Optional<Vendor> findByFkUser(User fkUser);

    boolean existsByBusinessNameAndPkVendorIdNot(String businessName, UUID vendorId);

    @Query("SELECT new com.productadda.dto.admin.VendorAdminSummaryResponseDto(" +
            "v.pkVendorId, u.pkUserId, v.businessName, v.storeName, v.gstNumber, " +
            "v.businessDescription, u.email, CONCAT(u.firstName, ' ', u.lastName), " +
            "v.isActive, v.createdAtUtc) " +
            "FROM Vendor v JOIN v.fkUser u")
    Page<VendorAdminSummaryResponseDto> findAllAdminSummaries(Pageable pageable);
}