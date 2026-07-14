package com.productadda.service.invoice;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;

import com.productadda.dto.order.InvoiceResponseDto;
import com.productadda.entity.Address;
import com.productadda.entity.Invoice;
import com.productadda.entity.InvoiceLineItem;
import com.productadda.entity.Order;
import com.productadda.entity.OrderItem;
import com.productadda.entity.User;
import com.productadda.entity.UserRole;
import com.productadda.exception.ApiException;
import com.productadda.repository.InvoiceLineItemRepository;
import com.productadda.repository.InvoiceRepository;
import com.productadda.repository.OrderItemRepository;
import com.productadda.repository.OrderRepository;
import com.productadda.repository.UserRepository;
import com.productadda.repository.UserRoleRepository;
import com.productadda.util.UuidUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InvoiceGenerationService {

        private static final DateTimeFormatter INVOICE_NUMBER_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

        private final UserRepository userRepository;
        private final UserRoleRepository userRoleRepository;
        private final OrderRepository orderRepository;
        private final OrderItemRepository orderItemRepository;
        private final InvoiceRepository invoiceRepository;
        private final InvoiceLineItemRepository invoiceLineItemRepository;

        private final InvoicePdfRenderer invoicePdfRenderer;
        private final InvoiceStorageService invoiceStorageService;

        private final UuidUtil uuidUtil;

        /*
         * ================================================================
         * SECURE STAND-ALONE INVOICE GENERATION OR UPDATE
         * Description: Compiles invoice transaction data for a specific order.
         * Renders an unalterable PDF document via Thymeleaf and openhtmltopdf engines,
         * then provisions storage to a private cloud object bucket using Backblaze B2.
         * Enforces strict Level-2 tenant ownership isolation to guarantee standard
         * customers
         * can only generate assets matching their database identity profile records.
         * ================================================================
         */
        @Transactional
        public InvoiceResponseDto generateInvoice(UUID orderId) {

                /*
                 * ================================================================
                 * 1. VALIDATION SECTION
                 * ================================================================
                 */

                // ==========================================
                // 1.1 REQUEST VALIDATION
                // ==========================================
                if (orderId == null) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Order id is required to generate an invoice");
                }

                // ==========================================
                // 1.2 CONTEXT AUTHENTICATION
                // ==========================================
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication == null || !authentication.isAuthenticated()) {
                        throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentication missing or invalid");
                }

                String currentUsername = authentication.getName();

                // ==========================================
                // 1.3 DATABASE LOOKUP VALIDATION (LEVEL-2 ZERO TRUST)
                // ==========================================
                User currentUser = userRepository.findByEmail(currentUsername)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                "Authenticated user context no longer exists"));

                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));

                List<UserRole> userRoles = userRoleRepository.findByFkUser(currentUser);
                
                if (userRoles.isEmpty()) {
                        throw new ApiException(HttpStatus.FORBIDDEN,
                                        "User lacks privileges to execute this generation stream");
                }

                boolean isAdminOrSuperAdmin = userRoles.stream()
                                .map(role -> role.getFkRole().getRoleName())
                                .anyMatch(name -> "ADMIN".equals(name) || "SUPER_ADMIN".equals(name));

                // Strict Tenancy Symmetrical Guardrail: Standard customer identities must
                // explicitly
                // match the owner of the resource record pulled from the database.
                if (!isAdminOrSuperAdmin && !order.getFkUser().getPkUserId().equals(currentUser.getPkUserId())) {
                        throw new ApiException(HttpStatus.FORBIDDEN,
                                        "Access denied. Order context belongs to another customer entity");
                }

                String currentOrderHash = calculateOrderHash(order);

                Invoice existingInvoice = invoiceRepository.findByFkOrder_PkOrderId(order.getPkOrderId()).orElse(null);

                if (existingInvoice != null) {
                        if (existingInvoice.getOrderSnapshotHash().equals(currentOrderHash)) {
                                throw new ApiException(HttpStatus.CONFLICT,
                                                "Invoice is already up to date. No changes detected.");
                        }
                }

                List<OrderItem> orderItems = orderItemRepository.findByFkOrderAndIsActiveTrue(order);

                if (orderItems.isEmpty()) {
                        throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                                        "Cannot generate invoice for an order with no active line items");
                }

                /*
                 * ================================================================
                 * 2. BUSINESS RULES & PROCESSING / WORKFLOW
                 * ================================================================
                 */
                LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
                String invoiceNumber;
                int nextVersion;
                UUID targetInvoiceId;

                if (existingInvoice == null) {
                        invoiceNumber = "INV-" + now.format(INVOICE_NUMBER_DATE_FORMAT) + "-"
                                        + uuidUtil.generateUuidV7String().replace("-", "").substring(0, 6)
                                                        .toUpperCase();
                        nextVersion = 1;
                        targetInvoiceId = uuidUtil.generateUuidV7();
                } else {
                        invoiceNumber = existingInvoice.getInvoiceNumber();
                        nextVersion = existingInvoice.getInvoiceVersion() + 1;
                        targetInvoiceId = existingInvoice.getPkInvoiceId();
                }

                Context templateContext = buildInvoiceTemplateContext(order, orderItems, invoiceNumber, nextVersion,
                                now);

                byte[] pdfBytes = invoicePdfRenderer.renderInvoicePdf(templateContext);

                String objectKey = "invoices/" + invoiceNumber + (nextVersion == 1 ? "" : "-v" + nextVersion) + ".pdf";

                invoiceStorageService.uploadInvoicePdf(objectKey, pdfBytes);

                Invoice invoiceToSave;
                if (existingInvoice == null) {
                        invoiceToSave = Invoice.builder()
                                        .pkInvoiceId(targetInvoiceId)
                                        .fkOrder(order)
                                        .invoiceNumber(invoiceNumber)
                                        .invoiceVersion(nextVersion)
                                        .invoiceAmount(order.getTotalAmount())
                                        .fileUrl(null)
                                        .objectKey(objectKey)
                                        .orderSnapshotHash(currentOrderHash)
                                        .generatedAt(now)
                                        .isActive(true)
                                        .build();
                } else {
                        invoiceToSave = existingInvoice;
                        invoiceToSave.setInvoiceVersion(nextVersion);
                        invoiceToSave.setInvoiceAmount(order.getTotalAmount());
                        invoiceToSave.setObjectKey(objectKey);
                        invoiceToSave.setOrderSnapshotHash(currentOrderHash);
                        invoiceToSave.setGeneratedAt(now);
                }

                /*
                 * ================================================================
                 * 3. DB SAVING SECTION
                 * ================================================================
                 */
                Invoice savedInvoice = invoiceRepository.save(invoiceToSave);

                if (nextVersion == 1) {
                        for (OrderItem item : orderItems) {
                                InvoiceLineItem lineItem = InvoiceLineItem.builder()
                                                .pkInvoiceLineItemId(uuidUtil.generateUuidV7())
                                                .fkInvoice(savedInvoice)
                                                .fkOrderItem(item)
                                                .itemDescription(item.getProductNameSnapshot() + " (Qty: "
                                                                + item.getQuantity() + ")")
                                                .quantity(item.getQuantity())
                                                .unitPrice(item.getUnitPrice())
                                                .lineAmount(item.getLineTotal())
                                                .isActive(true)
                                                .build();

                                invoiceLineItemRepository.save(lineItem);
                        }
                }

                // TODO: Wire unified background messaging notification dispatchers here
                // to forward completed compilation payloads safely to client communications
                // layers.

                /*
                 * ================================================================
                 * 4. POST-SAVING DATA SANITIZATION & MASKING
                 * ================================================================
                 */
                // String activePresignedUrl = invoiceStorageService.generatePresignedUrl(savedInvoice.getObjectKey(),
                //                 Duration.ofMinutes(15));

                /*
                 * ================================================================
                 * 5. RESPONSE MAPPING
                 * ================================================================
                 */
                return InvoiceResponseDto.builder()
                                .invoiceId(savedInvoice.getPkInvoiceId())
                                .invoiceNumber(savedInvoice.getInvoiceNumber())
                                .invoiceVersion(savedInvoice.getInvoiceVersion())
                                .orderId(order.getPkOrderId())
                                .invoiceAmount(savedInvoice.getInvoiceAmount())
                                .generatedAt(savedInvoice.getGeneratedAt())
                                .fileUrl(savedInvoice.getFileUrl())
                                // .presignedUrl(activePresignedUrl)
                                .build();
        }

        /*
         * ================================================================
         * PRIVATE HELPER: CALCULATE ORDER HASH
         * Description: Computes a deterministic layout string fingerprint combining
         * subtotal,
         * tax, shipping costs, and final gross totals to securely verify incoming delta
         * adjustments.
         * ================================================================
         */
        private String calculateOrderHash(Order order) {
                return String.format("%s|%s|%s|%s",
                                order.getSubtotal(),
                                order.getTaxAmount(),
                                order.getShippingCost(),
                                order.getTotalAmount());
        }

        /*
         * ================================================================
         * PRIVATE HELPER: BUILD INVOICE TEMPLATE CONTEXT
         * Description: Assembles structural customer identification details and
         * geographical shipping
         * address parameters directly into a dynamic Thymeleaf processing model map
         * context.
         * ================================================================
         */
        private Context buildInvoiceTemplateContext(Order order, List<OrderItem> orderItems, String invoiceNumber,
                        int invoiceVersion, LocalDateTime generatedAt) {

                Context context = new Context();

                context.setVariable("invoiceNumber", invoiceNumber);
                context.setVariable("invoiceVersion", invoiceVersion);
                context.setVariable("generatedAt", generatedAt.toString());
                context.setVariable("orderNumber", order.getOrderNumber());

                context.setVariable("customerName", order.getFkUser() != null
                                ? order.getFkUser().getFirstName() + " " + order.getFkUser().getLastName()
                                : "Customer");

                Address address = order.getFkAddress();
                context.setVariable("addressLine1", address != null ? address.getAddressLine1() : "");
                context.setVariable("addressLine2", address != null ? address.getAddressLine2() : null);
                context.setVariable("city", address != null ? address.getCity() : "");
                context.setVariable("state", address != null ? address.getState() : "");
                context.setVariable("postalCode", address != null ? address.getPostalCode() : "");
                context.setVariable("country", address != null ? address.getCountry() : "");

                List<InvoiceLineTemplateItem> templateItems = orderItems.stream()
                                .map(item -> new InvoiceLineTemplateItem(
                                                item.getProductNameSnapshot() + " (Qty: " + item.getQuantity() + ")",
                                                item.getQuantity(),
                                                item.getUnitPrice(),
                                                item.getLineTotal()))
                                .toList();

                context.setVariable("items", templateItems);
                context.setVariable("subtotal", order.getSubtotal());
                context.setVariable("couponDiscount", order.getCouponDiscount());
                context.setVariable("shippingCost", order.getShippingCost());
                context.setVariable("taxAmount", order.getTaxAmount());
                context.setVariable("totalAmount", order.getTotalAmount());

                return context;
        }

        /*
         * ================================================================
         * PRIVATE STATIC RECORD: TEMPLATE-ONLY LINE ITEM VIEW
         * Description: Encapsulates structural calculations intended exclusively for
         * server-side
         * HTML markup expansion. This does not function as an external API payload
         * container.
         * ================================================================
         */
        private record InvoiceLineTemplateItem(
                        String description,
                        Integer quantity,
                        java.math.BigDecimal unitPrice,
                        java.math.BigDecimal lineAmount) {
        }
}