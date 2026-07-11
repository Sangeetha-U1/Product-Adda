package com.productadda.service.order;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.order.DeliveryAssignmentRejectResponseDto;
import com.productadda.dto.order.DeliveryAssignmentResponseDto;

import com.productadda.entity.DeliveryAssignment;
import com.productadda.entity.DeliveryAssignmentStatus;
import com.productadda.entity.DeliveryPartner;
import com.productadda.entity.Order;
import com.productadda.entity.OrderStatus;
import com.productadda.entity.User;
import com.productadda.entity.UserRole;

import com.productadda.exception.ApiException;

import com.productadda.repository.DeliveryAssignmentRepository;
import com.productadda.repository.DeliveryAssignmentStatusRepository;
import com.productadda.repository.DeliveryPartnerRepository;
import com.productadda.repository.OrderRepository;
import com.productadda.repository.OrderStatusRepository;
import com.productadda.repository.UserRepository;
import com.productadda.repository.UserRoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeliveryPartnerAcceptanceService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final DeliveryPartnerRepository deliveryPartnerRepository;
    private final DeliveryAssignmentRepository deliveryAssignmentRepository;
    private final DeliveryAssignmentStatusRepository deliveryAssignmentStatusRepository;
    private final OrderRepository orderRepository;
    private final OrderStatusRepository orderStatusRepository;

    /*
     * ================================================================
     * ACCEPT ASSIGNMENT
     * Description: Delivery-partner-only. Accepts a PENDING assignment
     * belonging to the authenticated partner, moving the order status
     * to OUT_FOR_DELIVERY. active_deliveries is intentionally left
     * unchanged (per the Day 5 plan, it only decrements on delivery
     * completion, which is out of scope for today).
     * ================================================================
     */
    @Transactional
    public DeliveryAssignmentResponseDto acceptAssignment(UUID assignmentId) {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        if (assignmentId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "assignmentId is required");
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
        DeliveryPartner partner = resolveAuthenticatedDeliveryPartner(currentUsername);

        DeliveryAssignment assignment = deliveryAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Assignment not found"));

        if (assignment.getFkDeliveryPartner() == null
                || !assignment.getFkDeliveryPartner().getPkDeliveryPartnerId().equals(partner.getPkDeliveryPartnerId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You do not have permission to accept this assignment");
        }

        String currentAssignmentStatusName = assignment.getFkAssignmentStatus() != null
                ? assignment.getFkAssignmentStatus().getStatusName()
                : null;

        if (!"PENDING".equals(currentAssignmentStatusName)) {
            throw new ApiException(HttpStatus.CONFLICT, "Assignment is already " + currentAssignmentStatusName);
        }

        DeliveryAssignmentStatus acceptedStatus = deliveryAssignmentStatusRepository.findByStatusName("ACCEPTED")
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "ACCEPTED delivery assignment status lookup row not found"));

        OrderStatus outForDeliveryStatus = orderStatusRepository.findByStatusName("OUT_FOR_DELIVERY")
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "OUT_FOR_DELIVERY order status lookup row not found"));

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        assignment.setFkAssignmentStatus(acceptedStatus);
        assignment.setAcceptedAt(now);
        assignment.setRejectionReason(null);

        Order order = assignment.getFkOrder();
        order.setFkStatus(outForDeliveryStatus);

        // TODO: Send notification (Week 8).
        // Example: notificationService.sendAssignmentAcceptedNotification(
        //     order.getFkUser().getPkUserId(), order.getPkOrderId(), "Your delivery is on the way");

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * ================================================================
         */
        DeliveryAssignment savedAssignment = deliveryAssignmentRepository.save(assignment);
        orderRepository.save(order);

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
                .acceptedAt(savedAssignment.getAcceptedAt())
                .build();
    }

    /*
     * ================================================================
     * REJECT ASSIGNMENT
     * Description: Delivery-partner-only. Rejects a PENDING assignment
     * belonging to the authenticated partner, un-assigning the partner
     * from the order and decrementing active_deliveries. Order status
     * is left untouched (it was never changed at assignment time in
     * Day 4, and only changes on ACCEPT, not before).
     * ================================================================
     */
    @Transactional
    public DeliveryAssignmentRejectResponseDto rejectAssignment(UUID assignmentId, String rejectionReason) {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        if (assignmentId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "assignmentId is required");
        }
        if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "rejectionReason cannot be blank");
        }

        String safeRejectionReason = rejectionReason.trim();

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
        DeliveryPartner partner = resolveAuthenticatedDeliveryPartner(currentUsername);

        DeliveryAssignment assignment = deliveryAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Assignment not found"));

        if (assignment.getFkDeliveryPartner() == null
                || !assignment.getFkDeliveryPartner().getPkDeliveryPartnerId().equals(partner.getPkDeliveryPartnerId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You do not have permission to reject this assignment");
        }

        String currentAssignmentStatusName = assignment.getFkAssignmentStatus() != null
                ? assignment.getFkAssignmentStatus().getStatusName()
                : null;

        if (!"PENDING".equals(currentAssignmentStatusName)) {
            throw new ApiException(HttpStatus.CONFLICT, "Assignment is already " + currentAssignmentStatusName);
        }

        DeliveryAssignmentStatus rejectedStatus = deliveryAssignmentStatusRepository.findByStatusName("REJECTED")
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "REJECTED delivery assignment status lookup row not found"));

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        assignment.setFkAssignmentStatus(rejectedStatus);
        assignment.setRejectedAt(now);
        assignment.setRejectionReason(safeRejectionReason);

        Order order = assignment.getFkOrder();
        order.setFkDeliveryPartner(null);

        partner.setActiveDeliveries(Math.max(partner.getActiveDeliveries() - 1, 0));

        // TODO: Trigger re-assignment workflow or notify admin (Week 6 Day 4
        // assignment logic could be re-run here, or the order could be
        // flagged for manual admin re-assignment, Week 8).
        // Example: deliveryAssignmentService.triggerReassignment(order.getPkOrderId());
        // or: notificationService.alertAdminForReassignment(order.getPkOrderId(), safeRejectionReason);

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * ================================================================
         */
        DeliveryAssignment savedAssignment = deliveryAssignmentRepository.save(assignment);
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
        return DeliveryAssignmentRejectResponseDto.builder()
                .assignmentId(savedAssignment.getPkAssignmentId())
                .orderId(order.getPkOrderId())
                .partnerId(partner.getPkDeliveryPartnerId())
                .assignmentStatusName(savedAssignment.getFkAssignmentStatus().getStatusName())
                .rejectedAt(savedAssignment.getRejectedAt())
                .rejectionReason(savedAssignment.getRejectionReason())
                .build();
    }

    /*
     * ================================================================
     * PRIVATE HELPER: RESOLVE AUTHENTICATED DELIVERY PARTNER
     * Description: Shared DB-level role and profile resolution used by
     * both methods in this service, mirroring
     * OrderStatusTransitionService's resolveAuthenticatedVendor helper.
     * Requires delivery_partners.fk_user_id to be manually linked (see
     * CHANGES_DAY5.md item 1) -- if not linked, this correctly falls
     * through to the same 403 a role-mismatched user would get.
     * ================================================================
     */
    private DeliveryPartner resolveAuthenticatedDeliveryPartner(String username) {

        User currentUser = userRepository.findByEmail(username)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "Authenticated user no longer exists"));

        List<UserRole> userRoles = userRoleRepository.findByFkUser(currentUser);

        if (userRoles.isEmpty()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Delivery partner role required to access this endpoint");
        }

        boolean hasDeliveryPartnerRole = userRoles.stream()
                .map(userRole -> userRole.getFkRole().getRoleName())
                .anyMatch(roleName -> "DELIVERY_PARTNER".equals(roleName));

        if (!hasDeliveryPartnerRole) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Delivery partner role required to access this endpoint");
        }

        return deliveryPartnerRepository.findByFkUser(currentUser)
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN,
                        "Delivery partner role required to access this endpoint"));
    }
}
