package com.productadda.service.notifications;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.entity.NotificationType;
import com.productadda.entity.Order;
import com.productadda.entity.Payment;
import com.productadda.entity.RecipientRole;
import com.productadda.entity.User;

import com.productadda.exception.ApiException;

import com.productadda.repository.NotificationTypeRepository;
import com.productadda.repository.RecipientRoleRepository;

import com.productadda.util.UuidUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventListenerServiceHandlePaymentSuccessful {

    // Admin high-value alert threshold, inherited from the original
    // plan ("notify admin if amount exceeds Rs. 10,000").
    private static final long HIGH_VALUE_THRESHOLD_IN_PAISE = 1_000_000L;

    private final NotificationTypeRepository notificationTypeRepository;
    private final RecipientRoleRepository recipientRoleRepository;
    private final NotificationCreationService notificationCreationService;
    private final RecipientResolverServiceGetOrderVendorUsers recipientResolverServiceGetOrderVendorUsers;
    private final RecipientResolverServiceGetAllAdminUsers recipientResolverServiceGetAllAdminUsers;
    private final UuidUtil uuidUtil;

    /*
     * ================================================================
     * HANDLE PAYMENT SUCCESSFUL
     * Description: Invoked directly from PaymentServicePaymentVerify
     * once payment verification succeeds. Distinct from
     * EventListenerServiceHandleOrderConfirmed -- this raises the
     * payment-receipt notification (PAYMENT_SUCCESS type), not the
     * order-confirmed notification, even though both fire from the
     * same call site and the same business moment. Notifies customer
     * and vendors always; admins only for high-value payments. Day 3:
     * amount_in_paise is now the raw paise value (not divided to
     * rupees), matching the plan's documented template variable.
     * ================================================================
     */
    @Transactional
    public void handlePaymentSuccessful(Order order, Payment payment) {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */
        if (order == null || order.getPkOrderId() == null || order.getFkUser() == null) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "A persisted order with a linked user is required to raise PAYMENT_SUCCESS");
        }
        if (payment == null || payment.getPkPaymentId() == null) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "A persisted payment is required to raise PAYMENT_SUCCESS");
        }

        NotificationType paymentSuccessType = notificationTypeRepository.findByNotificationTypeName("PAYMENT_SUCCESS")
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "PAYMENT_SUCCESS notification type lookup row not found"));

        RecipientRole customerRole = recipientRoleRepository.findByRoleName("CUSTOMER")
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "CUSTOMER recipient role lookup row not found"));

        RecipientRole vendorRole = recipientRoleRepository.findByRoleName("VENDOR")
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "VENDOR recipient role lookup row not found"));

        RecipientRole adminRole = recipientRoleRepository.findByRoleName("ADMIN")
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "ADMIN recipient role lookup row not found"));

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        UUID eventId = uuidUtil.generateUuidV7();

        Map<String, Object> context = new HashMap<>();
        context.put("customer_name", order.getFkUser().getFirstName());
        context.put("order_id", order.getOrderNumber());
        context.put("amount_in_paise", payment.getAmountInPaise());
        context.put("payment_method", payment.getPaymentMethod());
        context.put("transaction_id", payment.getGatewayTransactionId());

        List<User> vendorUsers = recipientResolverServiceGetOrderVendorUsers.getOrderVendorUsers(order);

        boolean isHighValue = payment.getAmountInPaise() != null
                && payment.getAmountInPaise() > HIGH_VALUE_THRESHOLD_IN_PAISE;

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * Reason: Delegated entirely to NotificationCreationService.
         * ================================================================
         */
        notificationCreationService.createNotificationsForRecipient(
                order.getFkUser(), customerRole, paymentSuccessType, eventId, order, payment, context);

        for (User vendorUser : vendorUsers) {
            notificationCreationService.createNotificationsForRecipient(
                    vendorUser, vendorRole, paymentSuccessType, eventId, order, payment, context);
        }

        if (isHighValue) {
            List<User> adminUsers = recipientResolverServiceGetAllAdminUsers.getAllAdminUsers();
            for (User adminUser : adminUsers) {
                notificationCreationService.createNotificationsForRecipient(
                        adminUser, adminRole, paymentSuccessType, eventId, order, payment, context);
            }
        }

        /*
         * ================================================================
         * 4. POST-SAVING DATA SANITIZATION & MASKING
         * Reason: N/A - void method, no response payload.
         * ================================================================
         */

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * Reason: N/A - internal event handler.
         * ================================================================
         */
    }
}
