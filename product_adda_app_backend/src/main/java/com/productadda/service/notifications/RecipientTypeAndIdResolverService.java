package com.productadda.service.notifications;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.productadda.dto.notifications.RecipientTypeAndIdDto;

import com.productadda.entity.DeliveryPartner;
import com.productadda.entity.RecipientType;
import com.productadda.entity.User;
import com.productadda.entity.Vendor;

import com.productadda.exception.ApiException;

import com.productadda.repository.DeliveryPartnerRepository;
import com.productadda.repository.RecipientTypeRepository;
import com.productadda.repository.VendorRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecipientTypeAndIdResolverService {

    private final RecipientTypeRepository recipientTypeRepository;
    private final VendorRepository vendorRepository;
    private final DeliveryPartnerRepository deliveryPartnerRepository;

    /*
     * ================================================================
     * RESOLVE
     * Description: Maps a (user, recipientRoleName) pair -- as stored
     * on `notifications` (fk_recipient_role: CUSTOMER/VENDOR/ADMIN/
     * DELIVERY_PARTNER) -- to the (recipient_type, recipient_id) pair
     * that `notification_preferences` is actually keyed by. CUSTOMER
     * and ADMIN both resolve to recipient_type=USER using the user's
     * own PK, since admins have no separate table. VENDOR and
     * DELIVERY_PARTNER resolve to their own table's PK via a lookup on
     * fk_user_id.
     * ================================================================
     */
    public RecipientTypeAndIdDto resolve(User user, String recipientRoleName) {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */
        if (user == null || user.getPkUserId() == null) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "user is required to resolve recipient type/id");
        }

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        String recipientTypeName;
        UUID recipientId;

        if ("VENDOR".equals(recipientRoleName)) {
            Vendor vendor = vendorRepository.findByFkUser(user)
                    .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Vendor record not found for user while resolving preferences"));
            recipientTypeName = "VENDOR";
            recipientId = vendor.getPkVendorId();
        } else if ("DELIVERY_PARTNER".equals(recipientRoleName)) {
            DeliveryPartner partner = deliveryPartnerRepository.findByFkUser(user)
                    .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Delivery partner record not found for user while resolving preferences"));
            recipientTypeName = "DELIVERY_PARTNER";
            recipientId = partner.getPkDeliveryPartnerId();
        } else {
            // CUSTOMER and ADMIN (and any future non-vendor/non-delivery-partner
            // role) both resolve to USER, using the user's own PK.
            recipientTypeName = "USER";
            recipientId = user.getPkUserId();
        }

        RecipientType recipientType = recipientTypeRepository.findByTypeName(recipientTypeName)
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        recipientTypeName + " recipient type lookup row not found"));

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * Reason: Read-only resolution, no mutations performed.
         * ================================================================
         */

        /*
         * ================================================================
         * 4. POST-SAVING DATA SANITIZATION & MASKING
         * Reason: N/A - see section 3.
         * ================================================================
         */

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * ================================================================
         */
        return RecipientTypeAndIdDto.builder()
                .recipientType(recipientType)
                .recipientId(recipientId)
                .build();
    }
}
