package com.productadda.service.notifications;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.notifications.NotificationContentDto;

import com.productadda.entity.DeliveryPartner;
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
public class EventListenerServiceHandleOrderPickedUp {

    private final NotificationTypeRepository notificationTypeRepository;
    private final RecipientRoleRepository recipientRoleRepository;
    private final NotificationContentBuilder notificationContentBuilder;
    private final NotificationCreationService notificationCreationService;
    private final UuidUtil uuidUtil;

    /*
     * ================================================================
     * HANDLE ORDER PICKED UP
     * Description: Invoked directly from
     * DeliveryPartnerPickupOrderService.pickupOrder() after the order
     * moves to PICKED_UP and the claiming partner is assigned. Notifies
     * customer and the claiming delivery partner.
     * ================================================================
     */
    @Transactional
    public void handleOrderPickedUp(Order order, DeliveryPartner partner) {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */
        if (order == null || order.getPkOrderId() == null || order.getFkUser() == null) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "A persisted order with a linked user is required to raise ORDER_PICKED_UP");
        }
        if (partner == null || partner.getFkUser() == null) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "A delivery partner with a linked user is required to raise ORDER_PICKED_UP");
        }

        NotificationType orderPickedUpType = notificationTypeRepository.findByNotificationTypeName("ORDER_PICKED_UP")
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "ORDER_PICKED_UP notification type lookup row not found"));

        RecipientRole customerRole = recipientRoleRepository.findByRoleName("CUSTOMER")
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "CUSTOMER recipient role lookup row not found"));

        RecipientRole deliveryPartnerRole = recipientRoleRepository.findByRoleName("DELIVERY_PARTNER")
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "DELIVERY_PARTNER recipient role lookup row not found"));

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        UUID eventId = uuidUtil.generateUuidV7();

        Map<String, String> context = new HashMap<>();
        context.put("customerName", order.getFkUser().getFirstName());
        context.put("orderNumber", order.getOrderNumber());
        context.put("partnerName", partner.getPartnerName());

        NotificationContentDto content = notificationContentBuilder.buildContent("ORDER_PICKED_UP", context);

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * Reason: Delegated entirely to NotificationCreationService.
         * ================================================================
         */
        notificationCreationService.createNotificationsForRecipient(
                order.getFkUser(), customerRole, orderPickedUpType, eventId, order, null, content);

        notificationCreationService.createNotificationsForRecipient(
                partner.getFkUser(), deliveryPartnerRole, orderPickedUpType, eventId, order, null, content);

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
