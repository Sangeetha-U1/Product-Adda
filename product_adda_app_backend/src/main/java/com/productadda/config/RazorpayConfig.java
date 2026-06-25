package com.productadda.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.razorpay.RazorpayClient;
import lombok.Getter;

@Configuration
@Getter
public class RazorpayConfig {

    @Value("${razorpay.key-id}")
    private String keyId;

    @Value("${razorpay.key-secret}")
    private String keySecret;

    @Bean
    public RazorpayClient razorpayClient() {
        try {
            return new RazorpayClient(keyId, keySecret);
        } catch (Exception e) {
            // Throwing a runtime exception prevents the app from starting up with a broken
            // gateway setup
            throw new IllegalStateException(
                    "Fatal: Failed to initialize RazorpayClient bean configuration. Check credentials.", e);
        }
    }
}