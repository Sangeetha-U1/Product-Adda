package com.productadda.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.productadda.entity.InvoiceLineItem;

public interface InvoiceLineItemRepository extends JpaRepository<InvoiceLineItem, UUID> {

    List<InvoiceLineItem> findByFkInvoice_PkInvoiceId(UUID invoiceId);
}
