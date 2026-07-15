package com.productadda.service.notifications;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.notifications.NotificationContentDto;

import com.productadda.entity.NotificationType;
import com.productadda.entity.Order;
import com.productadda.entity.RecipientRole;

import com.productadda.exception.ApiException;

import com.productadda.repository.NotificationTypeRepository;
import com.productadda.repository.RecipientRoleRepository;

import com.productadda.util.UuidUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventListenerServiceHandleOrderPlaced {

    private final NotificationTypeRepository notificationTypeRepository;
    private final RecipientRoleRepository recipientRoleRepository;
    private final NotificationContentBuilder notificationContentBuilder;
    private final NotificationCreationService notificationCreationService;
    private final UuidUtil uuidUtil;

    /*
     * ================================================================
     * HANDLE ORDER PLACED
     * Description: Invoked directly from OrderCreationService once a
     * new order has been persisted. Notifies the customer only --
     * per the agreed recipient mapping, vendors/admin are notified at
     * the PAID/ORDER_CONFIRMED transition instead, not at creation.
     * ================================================================
     */
    @Transactional
    public void handleOrderPlaced(Order order) {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */
        if (order == null || order.getPkOrderId() == null || order.getFkUser() == null) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "A persisted order with a linked user is required to raise ORDER_PLACED");
        }

        NotificationType orderPlacedType = notificationTypeRepository.findByNotificationTypeName("ORDER_PLACED")
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "ORDER_PLACED notification type lookup row not found"));

        RecipientRole customerRole = recipientRoleRepository.findByRoleName("CUSTOMER")
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "CUSTOMER recipient role lookup row not found"));

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        UUID eventId = uuidUtil.generateUuidV7();

        Map<String, String> context = new HashMap<>();
        context.put("customerName", order.getFkUser().getFirstName());
        context.put("orderNumber", order.getOrderNumber());
        context.put("totalAmount", String.valueOf(order.getTotalAmount()));

        NotificationContentDto content = notificationContentBuilder.buildContent("ORDER_PLACED", context);

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * Reason: Delegated entirely to NotificationCreationService, the
         * single write path for `notifications` rows.
         * ================================================================
         */
        notificationCreationService.createNotificationsForRecipient(
                order.getFkUser(), customerRole, orderPlacedType, eventId, order, null, content);

        /*
         * ================================================================
         * 4. POST-SAVING DATA SANITIZATION & MASKING
         * Reason: N/A - void method, no response payload.
         * ================================================================
         */

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * Reason: N/A - internal event handler, not bound to a REST
         * endpoint. Caller (OrderCreationService) continues its own flow.
         * ================================================================
         */
    }
}
