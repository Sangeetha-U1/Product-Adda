package com.productadda.service.notifications;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.notifications.NotificationContentDto;

import com.productadda.entity.NotificationType;
import com.productadda.entity.Order;
import com.productadda.entity.RecipientRole;
import com.productadda.entity.User;

import com.productadda.exception.ApiException;

import com.productadda.repository.NotificationTypeRepository;
import com.productadda.repository.RecipientRoleRepository;

import com.productadda.util.UuidUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventListenerServiceHandleOrderStatusAggregated {

    private final NotificationTypeRepository notificationTypeRepository;
    private final RecipientRoleRepository recipientRoleRepository;
    private final NotificationContentBuilder notificationContentBuilder;
    private final NotificationCreationService notificationCreationService;
    private final RecipientResolverServiceGetOrderVendorUsers recipientResolverServiceGetOrderVendorUsers;
    private final UuidUtil uuidUtil;

    /*
     * ================================================================
     * HANDLE ORDER STATUS AGGREGATED
     * Description: Invoked directly from
     * AggregatedStatusCalculationService.recalculateAggregatedStatus()
     * after it persists a new PROCESSING or SHIPPED order status.
     * Covers both VendorOrderStatusTransitionService (single item) and
     * VendorOrderStatusBatchTransitionService (batch) call sites, since
     * both funnel through the same aggregation method. Notifies
     * customer and every vendor on the order.
     * ================================================================
     */
    @Transactional
    public void handleOrderStatusAggregated(Order order, String aggregatedStatusName) {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */
        if (order == null || order.getPkOrderId() == null || order.getFkUser() == null) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "A persisted order with a linked user is required to raise an aggregated status notification");
        }
        if (!"PROCESSING".equals(aggregatedStatusName) && !"SHIPPED".equals(aggregatedStatusName)) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unsupported aggregated status for notification: " + aggregatedStatusName);
        }

        String notificationTypeName = "PROCESSING".equals(aggregatedStatusName)
                ? "ORDER_PROCESSING"
                : "ORDER_SHIPPED";

        NotificationType notificationType = notificationTypeRepository
                .findByNotificationTypeName(notificationTypeName)
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        notificationTypeName + " notification type lookup row not found"));

        RecipientRole customerRole = recipientRoleRepository.findByRoleName("CUSTOMER")
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "CUSTOMER recipient role lookup row not found"));

        RecipientRole vendorRole = recipientRoleRepository.findByRoleName("VENDOR")
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "VENDOR recipient role lookup row not found"));

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        UUID eventId = uuidUtil.generateUuidV7();

        Map<String, String> context = new HashMap<>();
        context.put("customerName", order.getFkUser().getFirstName());
        context.put("orderNumber", order.getOrderNumber());

        NotificationContentDto content = notificationContentBuilder.buildContent(notificationTypeName, context);

        List<User> vendorUsers = recipientResolverServiceGetOrderVendorUsers.getOrderVendorUsers(order);

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * Reason: Delegated entirely to NotificationCreationService.
         * ================================================================
         */
        notificationCreationService.createNotificationsForRecipient(
                order.getFkUser(), customerRole, notificationType, eventId, order, null, content);

        for (User vendorUser : vendorUsers) {
            notificationCreationService.createNotificationsForRecipient(
                    vendorUser, vendorRole, notificationType, eventId, order, null, content);
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
