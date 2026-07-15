package com.productadda.service.deliverypartners;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.deliverypartner.DeliveryPartnerClaimResponseDto;
import com.productadda.entity.DeliveryPartner;
import com.productadda.entity.Order;
import com.productadda.entity.OrderStatus;
import com.productadda.entity.User;
import com.productadda.entity.UserRole;

import com.productadda.exception.ApiException;

import com.productadda.repository.DeliveryPartnerRepository;
import com.productadda.repository.OrderRepository;
import com.productadda.repository.OrderStatusRepository;
import com.productadda.repository.UserRepository;
import com.productadda.repository.UserRoleRepository;
import com.productadda.service.notifications.EventListenerServiceHandleOrderPickedUp;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeliveryPartnerPickupOrderService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final DeliveryPartnerRepository deliveryPartnerRepository;
    private final OrderRepository orderRepository;
    private final OrderStatusRepository orderStatusRepository;

    private final EventListenerServiceHandleOrderPickedUp eventListenerServiceHandleOrderPickedUp;

    /*
     * ================================================================
     * Pickup ORDER
     * Description: Delivery-partner-only. Claims an available SHIPPED order
     * belonging to the authenticated partner, moving the order status
     * to PICKED_UP. active_deliveries is intentionally left
     * unchanged (per the plan, it only decrements on delivery
     * completion, which is out of scope for today).
     * ================================================================
     */
    @Transactional
    public DeliveryPartnerClaimResponseDto pickupOrder(UUID orderId) {

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
            throw new ApiException(HttpStatus.FORBIDDEN, "Delivery partner role required to access this endpoint");
        }

        boolean hasDeliveryPartnerRole = userRoles.stream()
                .map(userRole -> userRole.getFkRole().getRoleName())
                .anyMatch(roleName -> "DELIVERY_PARTNER".equals(roleName));

        if (!hasDeliveryPartnerRole) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Delivery partner role required to access this endpoint");
        }

        DeliveryPartner partner = deliveryPartnerRepository.findByFkUser(currentUser)
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN,
                        "Delivery partner role required to access this endpoint"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));

        if (order.getFkDeliveryPartner() != null) {
            throw new ApiException(HttpStatus.CONFLICT, "Order is already claimed by another delivery partner");
        }

        String orderStatusName = order.getFkStatus() != null
                ? order.getFkStatus().getStatusName()
                : null;

        if (!"SHIPPED".equals(orderStatusName)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Only SHIPPED orders can be claimed. Current status: " + orderStatusName);
        }

        OrderStatus pickedUpStatus = orderStatusRepository.findByStatusName("PICKED_UP")
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "PICKED_UP order status lookup row not found"));

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        order.setFkDeliveryPartner(partner);

        order.setFkStatus(pickedUpStatus);

        order.setPickedUpAtUtc(now);

        // this counter was never incremented on
        // claim despite the DB-saving comment below implying completion
        // should decrement it. Incrementing here so the completion-time
        // decrement has something to decrement.
        partner.setActiveDeliveries(partner.getActiveDeliveries() + 1);

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * ================================================================
         */
        orderRepository.save(order);

        deliveryPartnerRepository.save(partner);

        // raise ORDER_PICKED_UP notification
        eventListenerServiceHandleOrderPickedUp.handleOrderPickedUp(order, partner);

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
        return DeliveryPartnerClaimResponseDto.builder()
                .orderId(order.getPkOrderId())
                .partnerId(partner.getPkDeliveryPartnerId())
                .partnerName(partner.getPartnerName())
                .claimStatus("CLAIMED")
                .claimedAt(now)
                .build();
    }

}
