package com.productadda.repository;

import com.productadda.entity.Vendor;
import com.productadda.entity.VendorBankDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface VendorBankDetailRepository extends JpaRepository<VendorBankDetail, UUID> {
    Optional<VendorBankDetail> findByFkVendor(Vendor vendor);

    boolean existsByFkVendor(Vendor vendor);
}