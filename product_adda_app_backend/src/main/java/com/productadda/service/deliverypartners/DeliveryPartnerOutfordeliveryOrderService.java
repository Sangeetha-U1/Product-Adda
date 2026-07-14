package com.productadda.service.deliverypartners;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.security.SecureRandom;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.SendEmailRequestDto;
import com.productadda.dto.deliverypartner.DeliveryPartnerPickupResponseDto;
import com.productadda.entity.DeliveryPartner;
import com.productadda.entity.Order;
import com.productadda.entity.OrderStatus;
import com.productadda.entity.User;
import com.productadda.entity.UserRole;
import com.productadda.entity.DeliveryOtp;

import com.productadda.exception.ApiException;

import com.productadda.repository.DeliveryPartnerRepository;
import com.productadda.repository.OrderRepository;
import com.productadda.repository.OrderStatusRepository;
import com.productadda.repository.UserRepository;
import com.productadda.repository.UserRoleRepository;
import com.productadda.repository.DeliveryOtpRepository;

import com.productadda.service.mail.EmailService;

import com.productadda.util.HashUtil;
import com.productadda.util.UuidUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeliveryPartnerOutfordeliveryOrderService {

        private final UserRepository userRepository;
        private final UserRoleRepository userRoleRepository;
        private final DeliveryPartnerRepository deliveryPartnerRepository;
        private final OrderRepository orderRepository;
        private final OrderStatusRepository orderStatusRepository;
        private final DeliveryOtpRepository deliveryOtpRepository;

        private final EmailService emailService;

        private final HashUtil hashUtil;
        private final UuidUtil uuidUtil;

        /*
         * ================================================================
         * Outfordelivery ORDER
         * Description: Delivery-partner-only. Confirms the partner has left
         * the vendor/hub with the package, moving the order status from
         * PICKED_UP to OUT_FOR_DELIVERY.
         * ================================================================
         */
        @Transactional
        public DeliveryPartnerPickupResponseDto outforDeliveryOrder(UUID orderId) {

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

                // Zero-Trust ownership re-verification: this order must belong to
                // the authenticated partner, re-checked at the DB level rather than
                // trusted from any client-supplied context.
                if (order.getFkDeliveryPartner() == null
                                || !order.getFkDeliveryPartner().getPkDeliveryPartnerId()
                                                .equals(partner.getPkDeliveryPartnerId())) {
                        throw new ApiException(HttpStatus.FORBIDDEN, "Order is not assigned to you");
                }

                String orderStatusName = order.getFkStatus() != null
                                ? order.getFkStatus().getStatusName()
                                : null;

                if (!"PICKED_UP".equals(orderStatusName)) {
                        throw new ApiException(HttpStatus.CONFLICT,
                                        "Only PICKED_UP orders can move to OUT_FOR_DELIVERY. Current status: "
                                                        + orderStatusName);
                }

                OrderStatus outForDeliveryStatus = orderStatusRepository.findByStatusName("OUT_FOR_DELIVERY")
                                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                "OUT_FOR_DELIVERY order status lookup row not found"));

                /*
                 * ================================================================
                 * 2. BUSINESS RULES & PROCESSING / WORKFLOW
                 * ================================================================
                 */
                LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

                order.setFkStatus(outForDeliveryStatus);

                order.setOutForDeliveryAtUtc(now);

                String otpCode = String.valueOf(100000 + new SecureRandom().nextInt(900000));

                String otpHash = hashUtil.hashSha256(otpCode);

                // TODO: make this as var in app properties
                LocalDateTime otpExpiresAtUtc = now.plusMinutes(10);

                DeliveryOtp existingOtp = deliveryOtpRepository.findByFkOrder(order).orElse(null);

                DeliveryOtp deliveryOtp;

                if (existingOtp != null) {
                        existingOtp.setOtpHash(otpHash);
                        existingOtp.setExpiresAtUtc(otpExpiresAtUtc);
                        existingOtp.setIsUsed(false);
                        deliveryOtp = existingOtp;
                } else {
                        deliveryOtp = DeliveryOtp.builder()
                                        .pkDeliveryOtpId(uuidUtil.generateUuidV7())
                                        .fkOrder(order)
                                        .otpHash(otpHash)
                                        .expiresAtUtc(otpExpiresAtUtc)
                                        .isUsed(false)
                                        .isActive(true)
                                        .build();
                }

                /*
                 * ================================================================
                 * 3. DB SAVING SECTION
                 * ================================================================
                 */
                orderRepository.save(order);

                deliveryOtpRepository.save(deliveryOtp);
                /*
                 * ================================================================
                 * 4. EMAIL NOTIFICATION
                 * ================================================================
                 */
                String customerEmail = order.getFkUser().getEmail();

                String customerFirstName = order.getFkUser().getFirstName();

                SendEmailRequestDto sendEmailRequest = SendEmailRequestDto.builder()
                                .toEmail(customerEmail)
                                .subject("Your delivery verification code")
                                .body("""
                                                Hi %s,

                                                Your order is on its way! Please share the following one-time
                                                code with your delivery partner once your package arrives, to
                                                confirm you've received it:

                                                %s

                                                This code expires in 10 minutes.
                                                """.formatted(customerFirstName, otpCode))
                                .build();

                emailService.sendEmail(sendEmailRequest);

                /*
                 * ================================================================
                 * 5. POST-SAVING DATA SANITIZATION & MASKING
                 * ================================================================
                 */
                // No sensitive fields to strip; response is a minimal confirmation shape.

                /*
                 * ================================================================
                 * 5. RESPONSE MAPPING
                 * ================================================================
                 */
                return DeliveryPartnerPickupResponseDto.builder()
                                .orderId(order.getPkOrderId())
                                .partnerId(partner.getPkDeliveryPartnerId())
                                .partnerName(partner.getPartnerName())
                                .status("OUT_FOR_DELIVERY")
                                .outForDeliveryAtUtc(now)
                                .build();
        }

}
