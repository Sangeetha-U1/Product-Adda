package com.productadda.service.notifications;

import org.springframework.stereotype.Service;

import com.productadda.dto.SendEmailRequestDto;

import com.productadda.entity.Notification;

import com.productadda.service.mail.EmailService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationDispatchServiceEmail {

    private final EmailService emailService;

    /*
     * ================================================================
     * DISPATCH EMAIL
     * Description: Sends one notification's rendered title/message via
     * the existing SMTP-backed EmailService. Any exception from
     * EmailService (SMTP failure, bad config) propagates up to the
     * caller (NotificationQueueProcessorService), which is responsible
     * for catching it per-notification and applying retry/backoff --
     * this method does not swallow or interpret failures itself.
     * ================================================================
     */
    public void dispatchEmail(Notification notification) {

        String toEmail = notification.getFkUser().getEmail();

        SendEmailRequestDto sendEmailRequest = SendEmailRequestDto.builder()
                .toEmail(toEmail)
                .subject(notification.getTitle())
                .body(notification.getMessage())
                .build();

        emailService.sendEmail(sendEmailRequest);
    }
}
