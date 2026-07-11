package com.productadda.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.Invoice;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    // Places query lookups cleanly against a specific invoice token identifier
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    // Locates an active or historical invoice mapped directly to an isolated order
    // ID
    Optional<Invoice> findByFkOrder_PkOrderId(UUID orderId);

    // Navigates the relational graph to retrieve a pageable collection of active
    // invoices for a user
    Page<Invoice> findByFkOrder_FkUser_PkUserIdAndIsActiveTrue(UUID userId, Pageable pageable);
}