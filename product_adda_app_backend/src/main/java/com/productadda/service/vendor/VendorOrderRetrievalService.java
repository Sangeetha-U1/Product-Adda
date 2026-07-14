package com.productadda.service.vendor;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.vendor.PaginatedVendorOrderItemResponseDto;
import com.productadda.dto.vendor.VendorOrderItemDetailDto;
import com.productadda.dto.vendor.VendorOrderItemListDto;
import com.productadda.entity.Order;
import com.productadda.entity.OrderItem;
import com.productadda.entity.User;
import com.productadda.entity.UserRole;
import com.productadda.entity.Vendor;

import com.productadda.exception.ApiException;

import com.productadda.repository.OrderItemRepository;
import com.productadda.repository.UserRepository;
import com.productadda.repository.UserRoleRepository;
import com.productadda.repository.VendorRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VendorOrderRetrievalService {

        private final UserRepository userRepository;
        private final UserRoleRepository userRoleRepository;
        private final VendorRepository vendorRepository;
        private final OrderItemRepository orderItemRepository;

        /*
         * ================================================================
         * GET VENDOR ORDERS
         * Description: Retrieves a paginated list of orders containing at
         * least one item from the authenticated vendor. Within each order,
         * only this vendor's own items are visible (vendor isolation on
         * multi-vendor orders). Customer identity is masked to first name.
         * ================================================================
         */
        @Transactional(readOnly = true)
        public PaginatedVendorOrderItemResponseDto getVendorOrders(int page, int size) {

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
                // verifies the VENDOR authority claim embedded in the JWT at issue
                // time. This DB-level check independently re-verifies the role
                // against the current user_roles table, matching the same defense
                // pattern just applied to CustomerOrderRetrievalService, so a
                // revoked-but-not-yet-expired token cannot slip through.
                List<UserRole> userRoles = userRoleRepository.findByFkUser(currentUser);

                if (userRoles.isEmpty()) {
                        throw new ApiException(HttpStatus.FORBIDDEN,
                                        "Vendor role required to access this endpoint");
                }

                List<String> assignedRoleNames = userRoles.stream()
                                .map(userRole -> userRole.getFkRole().getRoleName())
                                .collect(Collectors.toList());

                boolean hasVendorRole = assignedRoleNames.stream()
                                .anyMatch(roleName -> "VENDOR".equals(roleName));

                if (!hasVendorRole) {
                        throw new ApiException(HttpStatus.FORBIDDEN,
                                        "Vendor role required to access this endpoint");
                }

                // NOTE: In addition to the role check above, this lookup enforces
                // that a Vendor profile actually exists for the authenticated user,
                // matching the existing VendorDashboardMetricsService precedent. A
                // user could theoretically hold the VENDOR authority without (yet)
                // completing vendor registration, so both checks are kept.
                Vendor vendor = vendorRepository.findByFkUser(currentUser)
                                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN,
                                                "Vendor role required to access this endpoint"));

                /*
                 * ================================================================
                 * 2. BUSINESS RULES & PROCESSING / WORKFLOW
                 * ================================================================
                 */
                PageRequest pageRequest = PageRequest.of(page, size,
                                Sort.by(Sort.Direction.DESC, OrderItem.SORT_BY_ORDER_CREATED_AT_UTC));

                Page<Order> orderPage = orderItemRepository.findDistinctOrdersByVendorId(
                                vendor.getPkVendorId(), pageRequest);

                /*
                 * ================================================================
                 * 3. DB SAVING SECTION (Skip if Read-Only GET)
                 * Reason: Read-only vendor-scoped lookup, no mutations performed.
                 * ================================================================
                 */

                /*
                 * ================================================================
                 * 4. POST-SAVING DATA SANITIZATION & MASKING
                 * ================================================================
                 */
                List<VendorOrderItemListDto> content = orderPage.getContent().stream()
                                .map(order -> {

                                        List<OrderItem> vendorItems = orderItemRepository
                                                        .findByFkOrderAndFkVendorAndIsActiveTrue(order, vendor);

                                        List<VendorOrderItemDetailDto> itemDtos = vendorItems.stream()
                                                        .map(item -> VendorOrderItemDetailDto.builder()
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
                                                                        // NOTE: Assumes ItemStatus exposes
                                                                        // getStatusName(),
                                                                        // mirroring the OrderStatus entity pattern used
                                                                        // elsewhere in this codebase. Unverified
                                                                        // against
                                                                        // the actual ItemStatus.java file.
                                                                        .itemStatusName(item.getFkItemStatus() != null
                                                                                        ? item.getFkItemStatus()
                                                                                                        .getStatusName()
                                                                                        : "UNKNOWN")
                                                                        .build())
                                                        .collect(Collectors.toList());

                                        // Mask customer identity: first name only, no email exposed.
                                        String customerFirstName = order.getFkUser() != null
                                                        ? order.getFkUser().getFirstName()
                                                        : "Unknown";

                                        return VendorOrderItemListDto.builder()
                                                        .orderId(order.getPkOrderId())
                                                        .orderNumber(order.getOrderNumber())
                                                        .orderStatusName(order.getFkStatus() != null
                                                                        ? order.getFkStatus().getStatusName()
                                                                        : "UNKNOWN")
                                                        .customerFirstName(customerFirstName)
                                                        .orderTotalAmount(order.getTotalAmount())
                                                        .createdAtUtc(order.getCreatedAtUtc())
                                                        .items(itemDtos)
                                                        .build();
                                })
                                .collect(Collectors.toList());

                /*
                 * ================================================================
                 * 5. RESPONSE MAPPING
                 * ================================================================
                 */
                return PaginatedVendorOrderItemResponseDto.builder()
                                .content(content)
                                .totalElements(orderPage.getTotalElements())
                                .page(page)
                                .size(size)
                                .build();
        }
}