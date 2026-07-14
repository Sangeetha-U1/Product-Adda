package com.productadda.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.Vendor;
import com.productadda.entity.VendorPayout;

public interface VendorPayoutRepository extends JpaRepository<VendorPayout, UUID> {

        // ==========================================
        // MOST RECENT PAYOUT FOR A VENDOR
        // Description: Used to determine the start of the next eligible
        // payout window - a vendor's next run only considers orders
        // delivered after their last payout's period_end_date, preventing
        // the same order from being paid out twice.
        // ==========================================
        Optional<VendorPayout> findTopByFkVendorOrderByPeriodEndDateDesc(Vendor vendor);
}
