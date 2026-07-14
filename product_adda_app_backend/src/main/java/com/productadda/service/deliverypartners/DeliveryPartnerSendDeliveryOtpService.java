package com.productadda.service.deliverypartners;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.SendEmailRequestDto;
import com.productadda.dto.order.DeliveryOtpSendResponseDto;

import com.productadda.entity.DeliveryOtp;
import com.productadda.entity.DeliveryPartner;
import com.productadda.entity.Order;
import com.productadda.entity.User;
import com.productadda.entity.UserRole;

import com.productadda.exception.ApiException;

import com.productadda.repository.DeliveryOtpRepository;
import com.productadda.repository.DeliveryPartnerRepository;
import com.productadda.repository.OrderRepository;
import com.productadda.repository.UserRepository;
import com.productadda.repository.UserRoleRepository;

import com.productadda.service.mail.EmailService;

import com.productadda.util.HashUtil;
import com.productadda.util.UuidUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeliveryPartnerSendDeliveryOtpService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final DeliveryPartnerRepository deliveryPartnerRepository;
    private final OrderRepository orderRepository;
    private final DeliveryOtpRepository deliveryOtpRepository;
    private final EmailService emailService;
    private final HashUtil hashUtil;
    private final UuidUtil uuidUtil;

    /*
     * ================================================================
     * SEND (OR RESEND) DELIVERY OTP
     * Description: Delivery-partner-only. Generates a fresh 6-digit OTP
     * for the given order, hashed at rest, valid for 10 minutes, and
     * emails the raw code to the customer. Calling this again for the
     * same order overwrites the previous OTP row (acts as "resend").
     * ================================================================
     */
    @Transactional
    public DeliveryOtpSendResponseDto sendDeliveryOtp(UUID orderId) {

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

        if (order.getFkDeliveryPartner() == null
                || !order.getFkDeliveryPartner().getPkDeliveryPartnerId().equals(partner.getPkDeliveryPartnerId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Order is not assigned to you");
        }

        String orderStatusName = order.getFkStatus() != null
                ? order.getFkStatus().getStatusName()
                : null;

        if (!"OUT_FOR_DELIVERY".equals(orderStatusName)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Delivery OTP can only be sent for OUT_FOR_DELIVERY orders. Current status: " + orderStatusName);
        }

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        String otpCode = String.valueOf(100000 + new SecureRandom().nextInt(900000));

        String otpHash = hashUtil.hashSha256(otpCode);

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

                        Your delivery partner has arrived. Please share the following
                        one-time code with them to confirm you've received your order:

                        %s

                        This code expires in 10 minutes.
                        """.formatted(customerFirstName, otpCode))
                .build();

        emailService.sendEmail(sendEmailRequest);

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * ================================================================
         */
        int atIndex = customerEmail.indexOf('@');
        String maskedEmail = atIndex > 2
                ? customerEmail.substring(0, 2) + "***" + customerEmail.substring(atIndex)
                : "***" + customerEmail.substring(atIndex);

        return DeliveryOtpSendResponseDto.builder()
                .orderId(order.getPkOrderId())
                .maskedEmail(maskedEmail)
                .expiresAtUtc(otpExpiresAtUtc)
                .message("OTP sent to customer's email")
                .build();
    }
}