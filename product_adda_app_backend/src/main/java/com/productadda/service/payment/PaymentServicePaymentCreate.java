package com.productadda.service.payment;

import java.math.BigDecimal;
import java.util.UUID;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

import com.productadda.config.RazorpayConfig;
import com.productadda.dto.payment.PaymentCreateRequestDto;
import com.productadda.dto.payment.PaymentCreateResponseDto;
import com.productadda.entity.Order;
import com.productadda.entity.Payment;
import com.productadda.entity.PaymentStatus;
import com.productadda.entity.User;
import com.productadda.exception.ApiException;
import com.productadda.repository.OrderRepository;
import com.productadda.repository.PaymentRepository;
import com.productadda.repository.PaymentStatusRepository;
import com.productadda.repository.UserRepository;
import com.productadda.util.UuidUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentServicePaymentCreate {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentStatusRepository paymentStatusRepository;
    private final UserRepository userRepository; // Isolated layer DB verification
    private final RazorpayConfig razorpayConfig;
    private final UuidUtil uuidUtil;

    public PaymentCreateResponseDto paymentCreate(PaymentCreateRequestDto requestDto) {
        try {

            /*
             * ================================================================
             * 1. ISOLATED SECURITY & VALIDATION SECTION
             * Description: Independent database verification. Even if filters are
             * bypassed, this block guarantees data integrity and strict ownership.
             * ================================================================
             */
            // ==========================================
            // 1.1 Read email from Security Context
            // ==========================================
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentication missing or invalid");
            }

            String email;
            if (authentication.getPrincipal() instanceof UserDetails userDetails) {
                email = userDetails.getUsername(); // Look, no manual casting!
            } else {
                email = authentication.getPrincipal().toString();
            }
            // ==========================================
            // 1.2 Isolated Database Check: Ensure user exists and is up to date in DB right
            // now
            // ==========================================
            User currentUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Authenticated user no longer exists"));

            // ==========================================
            // 1.3 Request Formatting
            // ==========================================
            UUID orderId = UUID.fromString(requestDto.getOrderId());

            // ==========================================
            // 1.4 Database Check: Fetch target order
            // ==========================================
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));

            // ==========================================
            // 1.5 Strict Ownership Validation: Cross-verify IDs straight from the DB
            // records
            // ==========================================
            if (order.getFkUser() == null || !order.getFkUser().getPkUserId().equals(currentUser.getPkUserId())) {
                throw new ApiException(HttpStatus.FORBIDDEN, "Access denied: This order does not belong to user "
                        + email + "with user id" + currentUser.getPkUserId());
            }

            // ==========================================
            // 1.6 Fetch Pending Status
            // ==========================================
            PaymentStatus pendingStatus = paymentStatusRepository.findByStatusName("PENDING")
                    .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Payment status PENDING not found"));

            /*
             * ================================================================
             * 2. BUSINESS SECTION (Razorpay Integration)
             * ================================================================
             */
            RazorpayClient razorpayClient = new RazorpayClient(razorpayConfig.getKeyId(),
                    razorpayConfig.getKeySecret());

            long amountInPaise = order.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue();

            JSONObject linkOptions = new JSONObject();
            linkOptions.put("amount", amountInPaise);
            linkOptions.put("currency", "INR");
            linkOptions.put("description", "Verification Testing for Order " + orderId);
            linkOptions.put("reference_id", order.getPkOrderId().toString());
            linkOptions.put("callback_url", "https://example.com");
            linkOptions.put("callback_method", "get");

            com.razorpay.PaymentLink paymentLink = razorpayClient.paymentLink.create(linkOptions);
            String razorpayPaymentLinkId = paymentLink.get("id");

            Payment payment = Payment.builder()
                    .pkPaymentId(uuidUtil.generateUuidV7())
                    .fkOrder(order)
                    .paymentMethod("RAZORPAY")
                    .fkStatus(pendingStatus)
                    .amountPaid(order.getTotalAmount())
                    .razorpayOrderId(null)
                    .razorpayPaymentLinkId(razorpayPaymentLinkId)
                    .build();

            /*
             * ================================================================
             * 3. DB SAVING SECTION
             * ================================================================
             */
            paymentRepository.save(payment);

            /*
             * ================================================================
             * 4. RESPONSE SECTION
             * ================================================================
             */
            return PaymentCreateResponseDto.builder()
                    .rawLinkDetails(paymentLink.toJson().toMap())
                    .build();

        } catch (ApiException ex) {
            throw ex;
        } catch (RazorpayException ex) {
            String razorpayErrorMessage = ex.getMessage();
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Razorpay Integration Failure: " + razorpayErrorMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Failed to initialize payment process");
        }
    }
}