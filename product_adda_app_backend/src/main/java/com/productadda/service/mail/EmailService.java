package com.productadda.service.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.productadda.dto.SendEmailRequestDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

        @Value("${app.mail.smtp-enabled}")
        private boolean smtpEnabled;

        @Value("${spring.mail.username}")
        private String fromEmail;

        private final JavaMailSender javaMailSender;

        public void sendEmail(SendEmailRequestDto request) {

                /*
                 * ================================================================
                 * 1. VALIDATION SECTION
                 * Description: Verifies system configurations and prerequisites before
                 * executing email delivery logic.
                 * ================================================================
                 */

                // ==========================================
                // 1.1 REQUEST VALIDATION
                // Description: Checks if the SMTP feature flag is enabled globally.
                // ==========================================
                if (!smtpEnabled) {

                        System.out.println("""

                                        ===========================================================================
                                        SMTP DISABLED - EMAIL NOT SENT

                                        To      : %s
                                        Subject : %s

                                        BODY:

                                        %s

                                        ===========================================================================
                                        """
                                        .formatted(
                                                        request.getToEmail(),
                                                        request.getSubject(),
                                                        request.getBody()));

                        return;
                }

                /*
                 * ================================================================
                 * 2. BUSINESS SECTION
                 * Description: Maps the request data transfer object into a simple
                 * mail message instance and triggers the JavaMailSender API.
                 * ================================================================
                 */

                SimpleMailMessage message = new SimpleMailMessage();

                message.setFrom(
                                "Fusion5tech <"
                                                + fromEmail
                                                + ">");

                message.setTo(
                                request.getToEmail());

                message.setSubject(
                                request.getSubject());

                message.setText(
                                request.getBody());

                javaMailSender.send(message);

                /*
                 * ================================================================
                 * 3. DB SAVING SECTION
                 * Description: Reserved for database state updates or operation auditing.
                 * ================================================================
                 */

                /*
                 * ================================================================
                 * 4. RESPONSE SECTION
                 * Description: Logs the process output status to console to signal
                 * execution termination.
                 * ================================================================
                 */

                // System.out.println("""

                // ===========================================================================
                // EMAIL SENT SUCCESSFULLY

                // To : %s
                // Subject : %s

                // ===========================================================================
                // """
                // .formatted(
                // request.getToEmail(),
                // request.getSubject(),
                // request.getBody()));
        }

}