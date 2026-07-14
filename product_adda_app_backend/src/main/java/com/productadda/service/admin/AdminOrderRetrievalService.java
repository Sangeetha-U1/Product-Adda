package com.productadda.service.admin;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.admin.AdminOrderDetailDto;
import com.productadda.dto.admin.PaginatedAdminOrderResponseDto;
import com.productadda.dto.order.OrderAddressSummaryDto;
import com.productadda.dto.order.OrderCouponSummaryDto;
import com.productadda.dto.order.OrderItemSummaryDto;
import com.productadda.dto.order.OrderPaymentSummaryDto;
import com.productadda.entity.Address;
import com.productadda.entity.Coupon;
import com.productadda.entity.DeliveryPartner;
import com.productadda.entity.Invoice;
import com.productadda.entity.Order;
import com.productadda.entity.Payment;
import com.productadda.entity.User;
import com.productadda.entity.UserRole;

import com.productadda.exception.ApiException;

import com.productadda.repository.InvoiceRepository;
import com.productadda.repository.OrderItemRepository;
import com.productadda.repository.OrderRepository;
import com.productadda.repository.PaymentRepository;
import com.productadda.repository.UserRepository;
import com.productadda.repository.UserRoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminOrderRetrievalService {

        private final UserRepository userRepository;
        private final UserRoleRepository userRoleRepository;
        private final OrderRepository orderRepository;
        private final OrderItemRepository orderItemRepository;
        private final PaymentRepository paymentRepository;
        private final InvoiceRepository invoiceRepository;

        /*
         * ================================================================
         * GET ADMIN ORDERS
         * Description: Retrieves a paginated, full-visibility list of ALL
         * orders across all customers and vendors, with advanced optional
         * filtering by status, customer, vendor, delivery partner, and
         * created-date range. Includes payment, delivery, and invoice
         * tracking details.
         * ================================================================
         */
        @Transactional(readOnly = true)
        public PaginatedAdminOrderResponseDto getAdminOrders(
                        int page, int size, String status, String userId, String vendorId,
                        String deliveryPartnerId, String createdAfter, String createdBefore) {

                /*
                 * ================================================================
                 * 1. VALIDATION SECTION
                 * ================================================================
                 */

                // ==========================================
                // 1.1 REQUEST VALIDATION
                // ==========================================
                if (page < 0) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "page must be >= 0");
                }
                if (size <= 0 || size > 100) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "size must be > 0 and <= 100");
                }

                String safeStatus = (status == null || status.trim().isEmpty())
                                ? null
                                : status.trim().toUpperCase();

                UUID safeUserId = null;
                if (userId != null && !userId.trim().isEmpty()) {
                        try {
                                safeUserId = UUID.fromString(userId.trim());
                        } catch (IllegalArgumentException ex) {
                                throw new ApiException(HttpStatus.BAD_REQUEST, "userId must be a valid UUID");
                        }
                }

                UUID safeVendorId = null;
                if (vendorId != null && !vendorId.trim().isEmpty()) {
                        try {
                                safeVendorId = UUID.fromString(vendorId.trim());
                        } catch (IllegalArgumentException ex) {
                                throw new ApiException(HttpStatus.BAD_REQUEST, "vendorId must be a valid UUID");
                        }
                }

                UUID safeDeliveryPartnerId = null;
                if (deliveryPartnerId != null && !deliveryPartnerId.trim().isEmpty()) {
                        try {
                                safeDeliveryPartnerId = UUID.fromString(deliveryPartnerId.trim());
                        } catch (IllegalArgumentException ex) {
                                throw new ApiException(HttpStatus.BAD_REQUEST,
                                                "deliveryPartnerId must be a valid UUID");
                        }
                }

                LocalDateTime createdAfterDateTime = null;
                if (createdAfter != null && !createdAfter.trim().isEmpty()) {
                        try {
                                createdAfterDateTime = LocalDate.parse(createdAfter.trim()).atStartOfDay();
                        } catch (DateTimeParseException ex) {
                                throw new ApiException(HttpStatus.BAD_REQUEST,
                                                "createdAfter must be a valid ISO date (yyyy-MM-dd)");
                        }
                }

                LocalDateTime createdBeforeDateTime = null;
                if (createdBefore != null && !createdBefore.trim().isEmpty()) {
                        try {
                                createdBeforeDateTime = LocalDate.parse(createdBefore.trim()).atTime(23, 59, 59);
                        } catch (DateTimeParseException ex) {
                                throw new ApiException(HttpStatus.BAD_REQUEST,
                                                "createdBefore must be a valid ISO date (yyyy-MM-dd)");
                        }
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
                // 1.3 DATABASE LOOKUP VALIDATION
                // ==========================================
                User currentUser = userRepository.findByEmail(currentUsername)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                "Authenticated user no longer exists"));

                // Zero-Trust role re-check: controller-level @PreAuthorize only
                // verifies the ADMIN/SUPER_ADMIN authority claim embedded in the
                // JWT at issue time. This DB-level check independently re-verifies
                // the role against the current user_roles table, matching the same
                // defense pattern already applied to CustomerOrderRetrievalService
                // and VendorOrderRetrievalService, so a revoked-but-not-yet-expired
                // admin token cannot slip through.
                List<UserRole> userRoles = userRoleRepository.findByFkUser(currentUser);

                if (userRoles.isEmpty()) {
                        throw new ApiException(HttpStatus.FORBIDDEN,
                                        "Admin role required to access this endpoint");
                }

                List<String> assignedRoleNames = userRoles.stream()
                                .map(userRole -> userRole.getFkRole().getRoleName())
                                .collect(Collectors.toList());

                boolean hasAdminPrivileges = assignedRoleNames.stream()
                                .anyMatch(roleName -> "ADMIN".equals(roleName) || "SUPER_ADMIN".equals(roleName));

                if (!hasAdminPrivileges) {
                        throw new ApiException(HttpStatus.FORBIDDEN,
                                        "Admin role required to access this endpoint");
                }

                List<UUID> vendorOrderIds = null;
                if (safeVendorId != null) {
                        vendorOrderIds = orderItemRepository.findDistinctOrderIdsByVendorId(safeVendorId);
                        if (vendorOrderIds.isEmpty()) {
                                // No orders exist for this vendor; short-circuit to an empty
                                // page rather than running a query guaranteed to match nothing.
                                return PaginatedAdminOrderResponseDto.builder()
                                                .content(List.of())
                                                .totalElements(0)
                                                .page(page)
                                                .size(size)
                                                .build();
                        }
                }

                /*
                 * ================================================================
                 * 2. BUSINESS RULES & PROCESSING / WORKFLOW
                 * ================================================================
                 */
                PageRequest pageRequest = PageRequest.of(page, size,
                                Sort.by(Sort.Direction.DESC, Order.SORT_BY_CREATED_AT_UTC));

                Page<Order> orderPage = orderRepository.findAdminOrdersWithFilters(
                                safeStatus, safeUserId, safeDeliveryPartnerId,
                                createdAfterDateTime, createdBeforeDateTime, vendorOrderIds, pageRequest);

                /*
                 * ================================================================
                 * 3. DB SAVING SECTION (Skip if Read-Only GET)
                 * Reason: Read-only admin lookup, no mutations performed.
                 * ================================================================
                 */

                /*
                 * ================================================================
                 * 4. POST-SAVING DATA SANITIZATION & MASKING
                 * ================================================================
                 */
                List<AdminOrderDetailDto> content = orderPage.getContent().stream()
                                .map(order -> {

                                        List<OrderItemSummaryDto> itemSummaries = orderItemRepository
                                                        .findByFkOrderAndIsActiveTrue(order)
                                                        .stream()
                                                        .map(item -> OrderItemSummaryDto.builder()
                                                                        .orderItemId(item.getPkOrderItemId())
                                                                        .productId(item.getFkProduct() != null
                                                                                        ? item.getFkProduct()
                                                                                                        .getPkProductId()
                                                                                        : null)
                                                                        .productNameSnapshot(
                                                                                        item.getProductNameSnapshot())
                                                                        .quantity(item.getQuantity())
                                                                        .unitPrice(item.getUnitPrice())
                                                                        .lineTotal(item.getLineTotal())
                                                                        .build())
                                                        .collect(Collectors.toList());

                                        Address address = order.getFkAddress();
                                        OrderAddressSummaryDto addressSummary = address == null
                                                        ? null
                                                        : OrderAddressSummaryDto.builder()
                                                                        .addressId(address.getPkAddressId())
                                                                        .addressLine1(address.getAddressLine1())
                                                                        .addressLine2(address.getAddressLine2())
                                                                        .landmark(address.getLandmark())
                                                                        .city(address.getCity())
                                                                        .state(address.getState())
                                                                        .postalCode(address.getPostalCode())
                                                                        .country(address.getCountry())
                                                                        .build();

                                        Coupon coupon = order.getFkCoupon();
                                        OrderCouponSummaryDto couponSummary = coupon == null
                                                        ? null
                                                        : OrderCouponSummaryDto.builder()
                                                                        .couponId(coupon.getPkCouponId())
                                                                        .couponCode(coupon.getCouponCode())
                                                                        .discountType(coupon.getFkDiscountType()
                                                                                        .getDiscountTypeCode())
                                                                        .discountValue(coupon.getDiscountValue())
                                                                        .build();

                                        Payment payment = paymentRepository.findByFkOrder(order).orElse(null);
                                        OrderPaymentSummaryDto paymentSummary = payment == null
                                                        ? null
                                                        : OrderPaymentSummaryDto.builder()
                                                                        .paymentId(payment.getPkPaymentId())
                                                                        .statusName(payment.getFkStatus()
                                                                                        .getStatusName())
                                                                        .paymentMethod(payment.getPaymentMethod())
                                                                        .amountPaidInPaise(payment.getAmountInPaise())
                                                                        .paidAtUtc(payment.getPaidAtUtc())
                                                                        .build();

                                        // TODO: Order.fkDeliveryPartner is currently mapped NOT NULL in
                                        // Order.java, but per Part 2's DB assessment, delivery partner
                                        // assignment happens after order confirmation via the separate
                                        // delivery_assignments accept/reject workflow. The user has
                                        // confirmed they will run the DB ALTER to make this column
                                        // nullable; once done, update Order.java's fk_delivery_partner_id
                                        // @JoinColumn to remove nullable = false, and this defensive
                                        // null-guard can be simplified (though it is safe to leave as-is).
                                        DeliveryPartner deliveryPartner = order.getFkDeliveryPartner();

                                        Invoice invoice = invoiceRepository
                                                        .findByFkOrder_PkOrderId(order.getPkOrderId()).orElse(null);

                                        return AdminOrderDetailDto.builder()
                                                        .orderId(order.getPkOrderId())
                                                        .orderNumber(order.getOrderNumber())
                                                        .statusName(order.getFkStatus() != null
                                                                        ? order.getFkStatus().getStatusName()
                                                                        : "UNKNOWN")
                                                        .customerId(order.getFkUser() != null
                                                                        ? order.getFkUser().getPkUserId()
                                                                        : null)
                                                        .customerFullName(order.getFkUser() != null
                                                                        ? order.getFkUser().getFirstName() + " "
                                                                                        + order.getFkUser()
                                                                                                        .getLastName()
                                                                        : "Unknown")
                                                        .customerEmail(order.getFkUser() != null
                                                                        ? order.getFkUser().getEmail()
                                                                        : null)
                                                        .subtotal(order.getSubtotal())
                                                        .couponDiscount(order.getCouponDiscount())
                                                        .shippingCost(order.getShippingCost())
                                                        .taxAmount(order.getTaxAmount())
                                                        .totalAmount(order.getTotalAmount())
                                                        .createdAtUtc(order.getCreatedAtUtc())
                                                        .items(itemSummaries)
                                                        .address(addressSummary)
                                                        .coupon(couponSummary)
                                                        .payment(paymentSummary)
                                                        .deliveryPartnerId(deliveryPartner != null
                                                                        ? deliveryPartner.getPkDeliveryPartnerId()
                                                                        : null)
                                                        .deliveryPartnerName(deliveryPartner != null
                                                                        ? deliveryPartner.getPartnerName()
                                                                        : null)
                                                        .invoiceId(invoice != null ? invoice.getPkInvoiceId()
                                                                        : null)
                                                        .invoiceNumber(invoice != null ? invoice.getInvoiceNumber()
                                                                        : null)
                                                        .invoiceAmount(invoice != null ? invoice.getInvoiceAmount()
                                                                        : null)
                                                        .invoiceGeneratedAt(invoice != null ? invoice.getGeneratedAt()
                                                                        : null)
                                                        // TODO: Populate cancellationReason once Order.java is
                                                        // extended with cancellation tracking fields (see the
                                                        // matching entity-level TODO and AdminOrderDetailDto TODO).
                                                        .cancellationReason(null)
                                                        .build();
                                })
                                .collect(Collectors.toList());

                /*
                 * ================================================================
                 * 5. RESPONSE MAPPING
                 * ================================================================
                 */
                return PaginatedAdminOrderResponseDto.builder()
                                .content(content)
                                .totalElements(orderPage.getTotalElements())
                                .page(page)
                                .size(size)
                                .build();
        }
}