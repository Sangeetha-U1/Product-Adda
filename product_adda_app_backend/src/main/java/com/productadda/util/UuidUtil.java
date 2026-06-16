package com.productadda.util;

import com.github.f4b6a3.uuid.UuidCreator;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class UuidUtil {

     public UUID generateUuidV7() {
        return UuidCreator.getTimeOrderedEpoch();
    }

    /**
     * Generates a time-ordered UUIDv7 string.
     * Ideal for database keys and secure random tokens.
     */
    public String generateUuidV7String() {
        return UuidCreator.getTimeOrderedEpoch().toString();
    }


}