package com.productadda.service.payment;

import java.math.BigDecimal;
import java.util.UUID;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.github.f4b6a3.uuid.UuidCreator;

import com.productadda.config.RazorpayConfig;
import com.productadda.dto.payment.PaymentCreateRequestDto;
import com.productadda.dto.payment.PaymentCreateResponseDto;
import com.productadda.entity.Order;
import com.productadda.entity.Payment;
import com.productadda.entity.PaymentStatus;
import com.productadda.exception.ApiException;
import com.productadda.repository.OrderRepository;
import com.productadda.repository.PaymentRepository;
import com.productadda.repository.PaymentStatusRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentServicePaymentCreate {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentStatusRepository paymentStatusRepository;
    private final RazorpayConfig razorpayConfig;

    public PaymentCreateResponseDto paymentCreate(PaymentCreateRequestDto requestDto) {
        try {

            /*
             * ================================================================
             * 1. VALIDATION SECTION
             * Description: Centralized block handling all input data integrity checks and
             * database lookups.
             * ================================================================
             */

            // ==========================================
            // 1.1 REQUEST VALIDATION
            // Description: Parses and ensures that incoming payload data parameters meet
            // fundamental requirements.
            // ==========================================
            UUID orderId = UUID.fromString(requestDto.getOrderId());
            
            // ==========================================
            // 1.2 DATABASE LOOKUP VALIDATION
            // Description: Verifies existence of dependent target records within the
            // database before running process logic.
            // ==========================================
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));

            PaymentStatus pendingStatus = paymentStatusRepository.findByStatusName("PENDING")
                    .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Payment status PENDING not found"));

            /*
             * ================================================================
             * 2. BUSINESS SECTION
             * Description: Initiates external integration with the Razorpay Payment
             * Gateway, calculates pricing units, and configures external link instances.
             * ================================================================
             */
            RazorpayClient razorpayClient = new RazorpayClient(razorpayConfig.getKeyId(),
                    razorpayConfig.getKeySecret());

            long amountInPaise = order.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue();

            JSONObject linkOptions = new JSONObject();
            linkOptions.put("amount", amountInPaise);
            linkOptions.put("currency", "INR");
            linkOptions.put("description", "Verification Testing for Order " + orderId);

            // Map your own DB Order UUID straight to the reference_id!
            linkOptions.put("reference_id", order.getPkOrderId().toString());
            linkOptions.put("callback_url", "https://example.com");
            linkOptions.put("callback_method", "get");

            // API call to Razorpay to get the hosted short_url
            com.razorpay.PaymentLink paymentLink = razorpayClient.paymentLink.create(linkOptions);

            String razorpayPaymentLinkId = paymentLink.get("id");

            Payment payment = Payment.builder()
                    .pkPaymentId(UuidCreator.getTimeOrderedEpoch())
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
             * Description: Executes persistence validations and stores the final local
             * payment entity to the database.
             * ================================================================
             */

            paymentRepository.save(payment);

            /*
             * ================================================================
             * 4. RESPONSE SECTION
             * Description: Extracts generation properties into structural payloads for API
             * presentation returns.
             * ================================================================
             */

            return PaymentCreateResponseDto.builder()
                    .rawLinkDetails(paymentLink.toJson().toMap())
                    .build();

        } catch (ApiException ex) {
            throw ex;
        } catch (RazorpayException ex) {

            // ex.printStackTrace();

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