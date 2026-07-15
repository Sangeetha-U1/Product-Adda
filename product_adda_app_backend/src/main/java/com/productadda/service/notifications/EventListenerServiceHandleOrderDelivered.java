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
public class EventListenerServiceHandleOrderDelivered {

    private final NotificationTypeRepository notificationTypeRepository;
    private final RecipientRoleRepository recipientRoleRepository;
    private final NotificationContentBuilder notificationContentBuilder;
    private final NotificationCreationService notificationCreationService;
    private final RecipientResolverServiceGetAllAdminUsers recipientResolverServiceGetAllAdminUsers;
    private final UuidUtil uuidUtil;

    /*
     * ================================================================
     * HANDLE ORDER DELIVERED
     * Description: Invoked directly from
     * DeliveryPartnerDeliveredOrderService.deliveredOrder() after the
     * order moves to DELIVERED. Notifies customer and all admins.
     * ================================================================
     */
    @Transactional
    public void handleOrderDelivered(Order order) {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */
        if (order == null || order.getPkOrderId() == null || order.getFkUser() == null) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "A persisted order with a linked user is required to raise ORDER_DELIVERED");
        }

        NotificationType orderDeliveredType = notificationTypeRepository.findByNotificationTypeName("ORDER_DELIVERED")
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "ORDER_DELIVERED notification type lookup row not found"));

        RecipientRole customerRole = recipientRoleRepository.findByRoleName("CUSTOMER")
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "CUSTOMER recipient role lookup row not found"));

        RecipientRole adminRole = recipientRoleRepository.findByRoleName("ADMIN")
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "ADMIN recipient role lookup row not found"));

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        UUID eventId = uuidUtil.generateUuidV7();

        Map<String, String> context = new HashMap<>();
        context.put("customerName", order.getFkUser().getFirstName());
        context.put("orderNumber", order.getOrderNumber());

        NotificationContentDto content = notificationContentBuilder.buildContent("ORDER_DELIVERED", context);

        List<User> adminUsers = recipientResolverServiceGetAllAdminUsers.getAllAdminUsers();

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * Reason: Delegated entirely to NotificationCreationService.
         * ================================================================
         */
        notificationCreationService.createNotificationsForRecipient(
                order.getFkUser(), customerRole, orderDeliveredType, eventId, order, null, content);

        for (User adminUser : adminUsers) {
            notificationCreationService.createNotificationsForRecipient(
                    adminUser, adminRole, orderDeliveredType, eventId, order, null, content);
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
