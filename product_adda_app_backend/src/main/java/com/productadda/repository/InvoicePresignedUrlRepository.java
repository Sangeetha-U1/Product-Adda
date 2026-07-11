package com.productadda.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.productadda.entity.InvoicePresignedUrl;

public interface InvoicePresignedUrlRepository extends JpaRepository<InvoicePresignedUrl, UUID> {
    // Find the active cache entry for the targeted invoice asset context
    Optional<InvoicePresignedUrl> findByFkInvoice_PkInvoiceIdAndIsActiveTrue(UUID invoiceId);
}