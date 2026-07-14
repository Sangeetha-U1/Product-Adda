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

import com.productadda.entity.DeliveryPartner;
import com.productadda.entity.Order;
import com.productadda.entity.OrderStatus;
import com.productadda.entity.User;
import com.productadda.entity.UserRole;
import com.productadda.dto.deliverypartner.DeliveryPartnerCompleteResponseDto;
import com.productadda.entity.DeliveryOtp;

import com.productadda.exception.ApiException;

import com.productadda.repository.DeliveryPartnerRepository;
import com.productadda.repository.OrderRepository;
import com.productadda.repository.OrderStatusRepository;
import com.productadda.repository.UserRepository;
import com.productadda.repository.UserRoleRepository;
import com.productadda.repository.DeliveryOtpRepository;

import com.productadda.util.HashUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeliveryPartnerDeliveredOrderService {

        private final UserRepository userRepository;
        private final UserRoleRepository userRoleRepository;
        private final DeliveryPartnerRepository deliveryPartnerRepository;
        private final OrderRepository orderRepository;
        private final OrderStatusRepository orderStatusRepository;
        private final DeliveryOtpRepository deliveryOtpRepository;
        private final HashUtil hashUtil;

        /*
         * ================================================================
         * Delivered ORDER
         * Description: Delivery-partner-only. Confirms the partner has
         * handed the package to the customer, moving the order status
         * from OUT_FOR_DELIVERY to DELIVERED. Decrements the partner's
         * activeDeliveries (floor 0) and increments totalDeliveries.
         * ================================================================
         */
        @Transactional
        public DeliveryPartnerCompleteResponseDto deliveredOrder(UUID orderId, String otp) {

                /*
                 * ================================================================
                 * 1. VALIDATION SECTION
                 * ================================================================
                 */
                if (orderId == null) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "orderId is required");
                }

                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication == null || !authentication.isAuthenticated()) {
                        throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentication missing or invalid");
                }

                String currentUsername = authentication.getName();

                User currentUser = userRepository.findByEmail(currentUsername)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                "Authenticated user no longer exists"));

                List<UserRole> userRoles = userRoleRepository.findByFkUser(currentUser);

                if (userRoles.isEmpty()) {
                        throw new ApiException(HttpStatus.FORBIDDEN,
                                        "Delivery partner role required to access this endpoint");
                }

                boolean hasDeliveryPartnerRole = userRoles.stream()
                                .map(userRole -> userRole.getFkRole().getRoleName())
                                .anyMatch(roleName -> "DELIVERY_PARTNER".equals(roleName));

                if (!hasDeliveryPartnerRole) {
                        throw new ApiException(HttpStatus.FORBIDDEN,
                                        "Delivery partner role required to access this endpoint");
                }

                DeliveryPartner partner = deliveryPartnerRepository.findByFkUser(currentUser)
                                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN,
                                                "Delivery partner role required to access this endpoint"));

                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));

                if (order.getFkDeliveryPartner() == null
                                || !order.getFkDeliveryPartner().getPkDeliveryPartnerId()
                                                .equals(partner.getPkDeliveryPartnerId())) {
                        throw new ApiException(HttpStatus.FORBIDDEN, "Order is not assigned to you");
                }

                String orderStatusName = order.getFkStatus() != null
                                ? order.getFkStatus().getStatusName()
                                : null;

                if (!"OUT_FOR_DELIVERY".equals(orderStatusName)) {
                        throw new ApiException(HttpStatus.CONFLICT,
                                        "Only OUT_FOR_DELIVERY orders can move to DELIVERED. Current status: "
                                                        + orderStatusName);
                }

                if (otp == null || otp.isBlank()) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "OTP is required to confirm delivery");
                }

                DeliveryOtp deliveryOtp = deliveryOtpRepository.findByFkOrder(order)
                                .orElseThrow(() -> new ApiException(HttpStatus.CONFLICT,
                                                "No delivery OTP has been generated for this order yet"));

                if (Boolean.TRUE.equals(deliveryOtp.getIsUsed())) {
                        throw new ApiException(HttpStatus.CONFLICT, "This OTP has already been used");
                }

                if (deliveryOtp.getExpiresAtUtc().isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
                        throw new ApiException(HttpStatus.CONFLICT, "OTP has expired. Please request a new one.");
                }

                if (!hashUtil.hashSha256(otp).equals(deliveryOtp.getOtpHash())) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid OTP");
                }

                OrderStatus deliveredStatus = orderStatusRepository.findByStatusName("DELIVERED")
                                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                "DELIVERED order status lookup row not found"));

                /*
                 * ================================================================
                 * 2. BUSINESS RULES & PROCESSING / WORKFLOW
                 * ================================================================
                 */
                LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

                order.setFkStatus(deliveredStatus);

                order.setDeliveredAtUtc(now);

                partner.setActiveDeliveries(Math.max(0, partner.getActiveDeliveries() - 1));

                partner.setTotalDeliveries(partner.getTotalDeliveries() + 1);

                deliveryOtp.setIsUsed(true);

                /*
                 * ================================================================
                 * 3. DB SAVING SECTION
                 * ================================================================
                 */
                orderRepository.save(order);

                deliveryPartnerRepository.save(partner);

                deliveryOtpRepository.save(deliveryOtp);

                /*
                 * ================================================================
                 * 4. POST-SAVING DATA SANITIZATION & MASKING
                 * ================================================================
                 */
                // No sensitive fields to strip; response is a minimal confirmation shape.

                /*
                 * ================================================================
                 * 5. RESPONSE MAPPING
                 * ================================================================
                 */
                return DeliveryPartnerCompleteResponseDto.builder()
                                .orderId(order.getPkOrderId())
                                .partnerId(partner.getPkDeliveryPartnerId())
                                .partnerName(partner.getPartnerName())
                                .status("DELIVERED")
                                .deliveredAtUtc(now)
                                .activeDeliveries(partner.getActiveDeliveries())
                                .totalDeliveries(partner.getTotalDeliveries())
                                .build();
        }
}
