package com.productadda.service.notifications;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.productadda.exception.ApiException;

@Service
public class TemplateSyntaxValidatorService {

    private static final int MAX_BODY_LENGTH = 10000;

    /*
     * ================================================================
     * VALIDATE
     * Description: Checks for balanced {{ }} pairs (no unclosed or
     * stray braces) and a reasonable length. Intentionally shallow --
     * this project uses plain {{variable}} substitution only, not real
     * Handlebars, so there is no conditional/loop syntax to validate.
     * ================================================================
     */
    public void validate(String text, String fieldName) {

        if (text == null || text.trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, fieldName + " must not be empty");
        }

        if (text.length() > MAX_BODY_LENGTH) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    fieldName + " must not exceed " + MAX_BODY_LENGTH + " characters");
        }

        int openCount = 0;
        int position = 0;

        while (position < text.length() - 1) {
            if (text.charAt(position) == '{' && text.charAt(position + 1) == '{') {
                openCount++;
                position += 2;
            } else if (text.charAt(position) == '}' && text.charAt(position + 1) == '}') {
                openCount--;
                if (openCount < 0) {
                    throw new ApiException(HttpStatus.BAD_REQUEST,
                            fieldName + " has an unmatched closing }} at position " + position);
                }
                position += 2;
            } else {
                position++;
            }
        }

        if (openCount > 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    fieldName + " has an unclosed {{ (missing closing }})");
        }
    }
}
