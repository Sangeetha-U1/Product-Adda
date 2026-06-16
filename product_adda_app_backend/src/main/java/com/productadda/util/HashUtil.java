package com.productadda.util;

import java.nio.charset.StandardCharsets;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HashUtil {

    @Value("${app.security.token-secret}")
    private String secretKey;

    public String hashSha256(String input) {
        if (input == null) {
            return null;
        }
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");

            // 2. Using your secretKey variable right here
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    secretKey.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256");
            sha256Hmac.init(secretKeySpec);

            byte[] encodedHash = sha256Hmac.doFinal(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder(2 * encodedHash.length);
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (Exception e) {
            throw new RuntimeException("Error generating HMAC-SHA256 hash", e);
        }
    }
}