package com.productadda.service.order;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.order.DeliveryAssignmentResponseDto;

import com.productadda.entity.DeliveryAssignment;
import com.productadda.entity.DeliveryAssignmentStatus;
import com.productadda.entity.DeliveryPartner;
import com.productadda.entity.Order;
import com.productadda.entity.User;
import com.productadda.entity.UserRole;

import com.productadda.exception.ApiException;

import com.productadda.repository.DeliveryAssignmentRepository;
import com.productadda.repository.DeliveryAssignmentStatusRepository;
import com.productadda.repository.DeliveryPartnerRepository;
import com.productadda.repository.OrderRepository;
import com.productadda.repository.UserRepository;
import com.productadda.repository.UserRoleRepository;

import com.productadda.util.UuidUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeliveryAssignmentService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final OrderRepository orderRepository;
    private final DeliveryPartnerRepository deliveryPartnerRepository;
    private final DeliveryAssignmentRepository deliveryAssignmentRepository;
    private final DeliveryAssignmentStatusRepository deliveryAssignmentStatusRepository;
    private final UuidUtil uuidUtil;

    // Delivery partner statuses considered eligible to receive a new
    // assignment. OFFLINE and ON_LEAVE partners are excluded. BUSY
    // partners are still eligible -- the separate active_deliveries vs
    // max_concurrent_deliveries capacity check is what actually gates
    // overload, not the status itself.
    private static final Set<String> ASSIGNABLE_PARTNER_STATUSES = Set.of("AVAILABLE", "BUSY");

    /*
     * ================================================================
     * ASSIGN DELIVERY PARTNER TO ORDER
     * Description: Admin-only. Assigns an active, under-capacity
     * delivery partner to a SHIPPED order not yet assigned, creating a
     * PENDING DeliveryAssignment awaiting partner acceptance.
     * ================================================================
     */
    @Transactional
    public DeliveryAssignmentResponseDto assignDeliveryPartnerToOrder(UUID orderId, UUID deliveryPartnerId) {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        if (orderId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "orderId is required");
        }
        if (deliveryPartnerId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "deliveryPartnerId is required");
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

        List<UserRole> userRoles = userRoleRepository.findByFkUser(currentUser);

        if (userRoles.isEmpty()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Admin role required to assign delivery partners");
        }

        boolean hasAdminPrivileges = userRoles.stream()
                .map(userRole -> userRole.getFkRole().getRoleName())
                .anyMatch(roleName -> "ADMIN".equals(roleName) || "SUPER_ADMIN".equals(roleName));

        if (!hasAdminPrivileges) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Admin role required to assign delivery partners");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));

        String orderStatusName = order.getFkStatus() != null ? order.getFkStatus().getStatusName() : null;

        if (!"SHIPPED".equals(orderStatusName)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Only SHIPPED orders can be assigned delivery partners. Current status: " + orderStatusName);
        }

        if (order.getFkDeliveryPartner() != null) {
            throw new ApiException(HttpStatus.CONFLICT, "Order already has a delivery partner assigned");
        }

        DeliveryPartner partner = deliveryPartnerRepository.findById(deliveryPartnerId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Delivery partner not found"));

        String partnerStatusName = partner.getFkStatus() != null ? partner.getFkStatus().getStatusName() : null;

        if (!ASSIGNABLE_PARTNER_STATUSES.contains(partnerStatusName)) {
            throw new ApiException(HttpStatus.CONFLICT, "Delivery partner is not active");
        }

        if (!Boolean.TRUE.equals(partner.getIsActive())) {
            throw new ApiException(HttpStatus.CONFLICT, "Delivery partner is not active");
        }

        if (partner.getActiveDeliveries() >= partner.getMaxConcurrentDeliveries()) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Delivery partner has reached maximum concurrent deliveries ("
                            + partner.getActiveDeliveries() + "/" + partner.getMaxConcurrentDeliveries() + ")");
        }

        DeliveryAssignmentStatus pendingAssignmentStatus = deliveryAssignmentStatusRepository
                .findByStatusName("PENDING")
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "PENDING delivery assignment status lookup row not found"));

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        UUID newAssignmentId = uuidUtil.generateUuidV7();

        DeliveryAssignment newAssignment = DeliveryAssignment.builder()
                .pkAssignmentId(newAssignmentId)
                .fkOrder(order)
                .fkDeliveryPartner(partner)
                .fkAssignmentStatus(pendingAssignmentStatus)
                .assignedAt(now)
                .acceptedAt(null)
                .rejectedAt(null)
                .completedAt(null)
                .rejectionReason(null)
                .isActive(true)
                .build();

        order.setFkDeliveryPartner(partner);

        partner.setActiveDeliveries(partner.getActiveDeliveries() + 1);

        // TODO: Send notification to delivery partner (Week 8).
        // Example: notificationService.sendAssignmentNotification(
        //     partner.getPkDeliveryPartnerId(), orderId, "New delivery pending your acceptance");

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * ================================================================
         */
        DeliveryAssignment savedAssignment = deliveryAssignmentRepository.save(newAssignment);
        orderRepository.save(order);
        deliveryPartnerRepository.save(partner);

        /*
         * ================================================================
         * 4. POST-SAVING DATA SANITIZATION & MASKING
         * ================================================================
         */
        // No sensitive fields to strip; response is already a minimal
        // confirmation shape.

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * ================================================================
         */
        return DeliveryAssignmentResponseDto.builder()
                .assignmentId(savedAssignment.getPkAssignmentId())
                .orderId(order.getPkOrderId())
                .partnerId(partner.getPkDeliveryPartnerId())
                .partnerName(partner.getPartnerName())
                .assignmentStatusName(savedAssignment.getFkAssignmentStatus().getStatusName())
                .assignedAt(savedAssignment.getAssignedAt())
                .build();
    }
}
